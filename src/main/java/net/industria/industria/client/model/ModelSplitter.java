package net.industria.industria.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.neoforged.neoforge.client.model.IQuadTransformer.*;

final class ModelSplitter {
    private static final List<Direction> FACINGS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    private ModelSplitter() {}

    static Map<Direction, Map<BlockPos, Section>> split(List<BakedQuad> quads, BlockPos size, boolean automaticCulling) {
        int[] dimensions = {size.getX(), size.getY(), size.getZ()};
        Map<BlockPos, List<Face>> north = new HashMap<>();
        for (BakedQuad quad : quads) {
            int[] data = quad.getVertices();
            Vertex a = Vertex.read(data, 0);
            Vertex b = Vertex.read(data, STRIDE);
            Vertex c = Vertex.read(data, 2 * STRIDE);
            Vertex d = Vertex.read(data, 3 * STRIDE);
            // Match Minecraft's quad triangulation to preserve interpolation even across
            // a UV or normal gradient that changes at the original diagonal.
            splitTriangle(List.of(a, b, c), quad, dimensions, automaticCulling, north);
            splitTriangle(List.of(c, d, a), quad, dimensions, automaticCulling, north);
        }

        Map<Direction, Map<BlockPos, Section>> variants = new EnumMap<>(Direction.class);
        for (Direction facing : FACINGS) {
            Map<BlockPos, Section> sections = new HashMap<>();
            north.forEach((pos, faces) -> sections.put(rotateCell(pos, size, facing), new Section(faces, facing)));
            variants.put(facing, Map.copyOf(sections));
        }
        return Map.copyOf(variants);
    }

    private static void splitTriangle(List<Vertex> triangle, BakedQuad source, int[] size, boolean automaticCulling,
                                      Map<BlockPos, List<Face>> sections) {
        Vector3f normal = cross(triangle.get(0), triangle.get(1), triangle.get(2));
        if (normal.lengthSquared() > 0) {
            splitAxis(triangle, 0, new int[3], size, normal, source, automaticCulling, sections);
        }
    }

    private static void splitAxis(List<Vertex> polygon, int axis, int[] cell, int[] size, Vector3f normal,
                                  BakedQuad source, boolean automaticCulling, Map<BlockPos, List<Face>> sections) {
        if (axis == 3) {
            addFragment(polygon, new BlockPos(cell[0], cell[1], cell[2]), normal, source, automaticCulling, sections);
            return;
        }

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (Vertex vertex : polygon) {
            float coordinate = vertex.position.get(axis);
            min = Math.min(min, coordinate);
            max = Math.max(max, coordinate);
        }
        if (max < 0 || min > size[axis]) {
            return;
        }

        int first = (int) Math.floor(min);
        int last = (int) Math.ceil(max) - 1;
        if (min == max) {
            // A face exactly on an integer plane belongs to the cell behind its winding
            // normal. Clamp at the structure bounds so even inward-facing shell faces survive.
            int owner = first;
            if (min == first && normal.get(axis) > 0) {
                owner--;
            }
            first = last = Math.clamp(owner, 0, size[axis] - 1);
        }

        // Slice one axis at a time so subsequent axes only visit cells reached by this slice.
        for (int index = Math.max(0, first); index <= Math.min(size[axis] - 1, last); index++) {
            List<Vertex> slice = clip(clip(polygon, axis, index, true), axis, index + 1, false);
            if (slice.size() >= 3) {
                cell[axis] = index;
                splitAxis(slice, axis + 1, cell, size, normal, source, automaticCulling, sections);
            }
        }
    }

    private static List<Vertex> clip(List<Vertex> polygon, int axis, float plane, boolean keepAbove) {
        if (polygon.isEmpty()) {
            return polygon;
        }

        // Sutherland–Hodgman clipping. The same edge fraction interpolates position,
        // atlas UV, color, packed light, and normals. Snap only the intersected axis.
        List<Vertex> result = new ArrayList<>(polygon.size() + 1);
        Vertex previous = polygon.getLast();
        float prevDistance = previous.position.get(axis) - plane;
        for (Vertex current : polygon) {
            float distance = current.position.get(axis) - plane;
            if ((prevDistance < 0 && distance > 0) || (prevDistance > 0 && distance < 0)) {
                float fraction = prevDistance / (prevDistance - distance);
                result.add(previous.interpolate(current, fraction, axis, plane));
            }
            if (keepAbove ? distance >= 0 : distance <= 0) {
                result.add(current);
            }
            previous = current;
            prevDistance = distance;
        }
        return result;
    }

    private static void addFragment(List<Vertex> polygon, BlockPos cell, Vector3f normal, BakedQuad source,
                                    boolean automaticCulling, Map<BlockPos, List<Face>> sections) {
        Direction cull = automaticCulling ? cullSide(polygon, normal, cell) : null;
        Vertex first = polygon.getFirst();
        // The clipped triangle is convex. Emit a triangle fan as quads with a repeated
        // last vertex, which is also how the NeoForge OBJ loader encodes triangles.
        for (int i = 1; i + 1 < polygon.size(); i++) {
            Vertex second = polygon.get(i);
            Vertex third = polygon.get(i + 1);
            if (cross(first, second, third).lengthSquared() == 0) {
                continue;
            }
            int[] data = new int[4 * STRIDE];
            first.write(data, 0, cell);
            second.write(data, STRIDE, cell);
            third.write(data, 2 * STRIDE, cell);
            System.arraycopy(data, 2 * STRIDE, data, 3 * STRIDE, STRIDE);
            sections.computeIfAbsent(cell, ignored -> new ArrayList<>()).add(new Face(copyQuad(source, data, source.getDirection()), cull));
        }
    }

    @Nullable
    private static Direction cullSide(List<Vertex> polygon, Vector3f normal, BlockPos cell) {
        Direction direction = Direction.getNearest(normal.x, normal.y, normal.z);
        int axis = direction.getAxis().ordinal();
        boolean positive = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        int boundary = cell.get(direction.getAxis()) + (positive ? 1 : 0);
        for (Vertex vertex : polygon) {
            if (vertex.position.get(axis) != boundary) {
                return null;
            }
        }
        return direction;
    }

    private static Vector3f cross(Vertex a, Vertex b, Vertex c) {
        return new Vector3f(b.position).sub(a.position).cross(new Vector3f(c.position).sub(a.position));
    }

    private static BakedQuad copyQuad(BakedQuad source, int[] data, Direction direction) {
        return new BakedQuad(data, source.getTintIndex(), direction, source.getSprite(), source.isShade(), source.hasAmbientOcclusion());
    }

    private static BlockPos rotateCell(BlockPos cell, BlockPos size, Direction facing) {
        int x = cell.getX();
        int y = cell.getY();
        int z = cell.getZ();
        // Rebase the rotated footprint at its minimum corner. East and west swap X and Z.
        return switch (facing) {
            case EAST -> new BlockPos(size.getZ() - 1 - z, y, x);
            case SOUTH -> new BlockPos(size.getX() - 1 - x, y, size.getZ() - 1 - z);
            case WEST -> new BlockPos(z, y, size.getX() - 1 - x);
            default -> cell;
        };
    }

    private static Direction rotateDirection(Direction direction, Direction facing) {
        if (direction.getAxis().isVertical()) {
            return direction;
        }
        return switch (facing) {
            case EAST -> direction.getClockWise();
            case SOUTH -> direction.getOpposite();
            case WEST -> direction.getCounterClockWise();
            default -> direction;
        };
    }

    private static BakedQuad rotateQuad(BakedQuad quad, Direction facing) {
        if (facing == Direction.NORTH) {
            return quad;
        }

        // With the cell already rotated, rotate its local vertices around the unit
        // square. The upper corner becomes the lower corner along any reflected axis.
        int[] data = quad.getVertices().clone();
        for (int offset = 0; offset < data.length; offset += STRIDE) {
            float x = Float.intBitsToFloat(data[offset + POSITION]);
            float z = Float.intBitsToFloat(data[offset + POSITION + 2]);
            Vector3f normal = unpackNormal(data[offset + NORMAL]);

            switch (facing) {
                case EAST -> {
                    writeXZ(data, offset, 1 - z, x);
                    data[offset + NORMAL] = packNormal(-normal.z, normal.y, normal.x);
                }
                case SOUTH -> {
                    writeXZ(data, offset, 1 - x, 1 - z);
                    data[offset + NORMAL] = packNormal(-normal.x, normal.y, -normal.z);
                }
                case WEST -> {
                    writeXZ(data, offset, z, 1 - x);
                    data[offset + NORMAL] = packNormal(normal.z, normal.y, -normal.x);
                }
            }
        }
        return copyQuad(quad, data, rotateDirection(quad.getDirection(), facing));
    }

    private static void writeXZ(int[] data, int offset, float x, float z) {
        data[offset + POSITION] = Float.floatToRawIntBits(x);
        data[offset + POSITION + 2] = Float.floatToRawIntBits(z);
    }

    private static Vector3f unpackNormal(int packed) {
        return new Vector3f((byte) packed, (byte) (packed >>> 8), (byte) (packed >>> 16));
    }

    private static int packNormal(float x, float y, float z) {
        return (Math.round(x) & 0xff) | (Math.round(y) & 0xff) << 8 | (Math.round(z) & 0xff) << 16;
    }

    private record Vertex(Vector3f position, Vector2f uv, Vector4f color, Vector3f normal, Vector2f light,
                          @Nullable Vector2f overlay) {
        static Vertex read(int[] data, int offset) {
            int color = data[offset + COLOR];
            return new Vertex(
                    new Vector3f(readFloat(data, offset + POSITION), readFloat(data, offset + POSITION + 1),
                            readFloat(data, offset + POSITION + 2)),
                    new Vector2f(readFloat(data, offset + UV0), readFloat(data, offset + UV0 + 1)),
                    new Vector4f(color & 0xff, (color >>> 8) & 0xff, (color >>> 16) & 0xff, color >>> 24),
                    unpackNormal(data[offset + NORMAL]),
                    unpackShorts(data[offset + UV2]),
                    UV1 < 0 ? null : unpackShorts(data[offset + UV1]));
        }

        Vertex interpolate(Vertex other, float fraction, int axis, float plane) {
            return new Vertex(new Vector3f(position).lerp(other.position, fraction).setComponent(axis, plane),
                    new Vector2f(uv).lerp(other.uv, fraction),
                    new Vector4f(color).lerp(other.color, fraction),
                    new Vector3f(normal).lerp(other.normal, fraction),
                    new Vector2f(light).lerp(other.light, fraction),
                    overlay == null ? null : new Vector2f(overlay).lerp(other.overlay, fraction));
        }

        void write(int[] data, int offset, BlockPos cell) {
            writeXZ(data, offset, position.x - cell.getX(), position.z - cell.getZ());
            data[offset + POSITION + 1] = Float.floatToRawIntBits(position.y - cell.getY());
            data[offset + UV0] = Float.floatToRawIntBits(uv.x);
            data[offset + UV0 + 1] = Float.floatToRawIntBits(uv.y);
            data[offset + COLOR] = Math.round(color.x) | Math.round(color.y) << 8
                    | Math.round(color.z) << 16 | Math.round(color.w) << 24;
            data[offset + NORMAL] = packNormal(normal.x, normal.y, normal.z);
            data[offset + UV2] = packShorts(light);
            if (overlay != null) {
                data[offset + UV1] = packShorts(overlay);
            }
        }

        private static float readFloat(int[] data, int index) {
            return Float.intBitsToFloat(data[index]);
        }

        private static Vector2f unpackShorts(int packed) {
            return new Vector2f(packed & 0xffff, packed >>> 16);
        }

        private static int packShorts(Vector2f value) {
            return Math.round(value.x) | Math.round(value.y) << 16;
        }
    }

    private record Face(BakedQuad quad, @Nullable Direction cull) {}

    static final class Section {
        private final List<BakedQuad> unculled;
        private final Map<Direction, List<BakedQuad>> culled;

        private Section(List<Face> faces, Direction facing) {
            List<BakedQuad> unculled = new ArrayList<>();
            Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
            for (Face face : faces) {
                BakedQuad quad = rotateQuad(face.quad, facing);
                if (face.cull == null) {
                    unculled.add(quad);
                } else {
                    Direction side = rotateDirection(face.cull, facing);
                    culled.computeIfAbsent(side, ignored -> new ArrayList<>()).add(quad);
                }
            }
            culled.replaceAll((side, quads) -> List.copyOf(quads));
            this.unculled = List.copyOf(unculled);
            this.culled = Map.copyOf(culled);
        }

        List<BakedQuad> getQuads(@Nullable Direction side) {
            return side == null ? unculled : culled.getOrDefault(side, List.of());
        }
    }
}
