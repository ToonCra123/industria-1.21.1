package net.industria.industria.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.neoforged.neoforge.client.model.IQuadTransformer.*;

public final class ModelSplitterTest {
    public static void main(String[] args) {
        clipsTriangleAcrossThreeAxes();
        rotatesRectangularFootprint();
        assignsOneOwnerToBoundaryFace();
        System.out.println("Split model geometry tests passed");
    }

    private static void clipsTriangleAcrossThreeAxes() {
        // This triangle follows z = x + y and crosses the x=1, y=1 and z=1 planes.
        BakedQuad source = quad(Direction.DOWN, new float[][]{
                {0, 0, 0}, {1.5f, 0, 1.5f}, {0, 1.5f, 1.5f}, {0, 1.5f, 1.5f}
        });
        var sections = ModelSplitter.split(List.of(source), new BlockPos(2, 2, 2), true).get(Direction.NORTH);
        check(sections.keySet().equals(Set.of(BlockPos.ZERO, new BlockPos(0, 0, 1),
                new BlockPos(1, 0, 1), new BlockPos(0, 1, 1))), "The triangle must cover exactly four cells");

        float area = 0;
        for (var entry : sections.entrySet()) {
            BlockPos cell = entry.getKey();
            for (BakedQuad fragment : entry.getValue().getQuads(null)) {
                check(fragment.getSprite() == source.getSprite() && fragment.getTintIndex() == source.getTintIndex()
                        && fragment.isShade() && fragment.hasAmbientOcclusion(), "Quad metadata changed");
                int[] data = fragment.getVertices();
                Vector3f[] corners = new Vector3f[3];
                for (int i = 0; i < corners.length; i++) {
                    int start = i * STRIDE;
                    Vector3f local = position(data, start);
                    check(Math.min(local.x, Math.min(local.y, local.z)) >= 0
                            && Math.max(local.x, Math.max(local.y, local.z)) <= 1, "A vertex escaped its block");
                    Vector3f world = new Vector3f(local).add(cell.getX(), cell.getY(), cell.getZ());
                    near(world.x + world.y, world.z, "Clipped vertex on source plane");
                    near(world.x / 2, readFloat(data, start + UV0), "Interpolated U");
                    near(world.y / 2, readFloat(data, start + UV0 + 1), "Interpolated V");
                    check(Math.abs((data[start + COLOR] & 0xff) - 100 * world.x) <= 1, "Interpolated color");
                    check(Math.abs((data[start + UV2] & 0xffff) - 100 * world.x) <= 1, "Interpolated light");
                    check(Math.abs((byte) data[start + NORMAL] - 42 * world.x) <= 1, "Interpolated normal");
                    corners[i] = local;
                }
                area += corners[1].sub(corners[0]).cross(corners[2].sub(corners[0])).length() / 2;
            }
        }
        near((float) (1.125 * Math.sqrt(3)), area, "Clipped surface area");
    }

    private static void rotatesRectangularFootprint() {
        // A marker on the north wall, in canonical cell (2, 0, 0) of a 3x1x2 model.
        BakedQuad marker = quad(Direction.NORTH, new float[][]{
                {2.25f, 0.25f, 0}, {2.25f, 0.75f, 0}, {2.75f, 0.75f, 0}, {2.75f, 0.25f, 0}
        });
        var variants = ModelSplitter.split(List.of(marker), new BlockPos(3, 1, 2), true);
        assertMarker(variants.get(Direction.NORTH), new BlockPos(2, 0, 0), Direction.NORTH, 0.25f, 0, 0x810000);
        assertMarker(variants.get(Direction.EAST), new BlockPos(1, 0, 2), Direction.EAST, 1, 0.25f, 0x00007f);
        assertMarker(variants.get(Direction.SOUTH), new BlockPos(0, 0, 1), Direction.SOUTH, 0.75f, 1, 0x7f0000);
        assertMarker(variants.get(Direction.WEST), BlockPos.ZERO, Direction.WEST, 0, 0.75f, 0x000081);
    }

    private static void assertMarker(Map<BlockPos, ModelSplitter.Section> sections, BlockPos cell, Direction side,
                                     float x, float z, int normal) {
        check(sections.keySet().equals(Set.of(cell)), "Incorrect cell after rotation to " + side);
        ModelSplitter.Section section = sections.get(cell);
        check(section.getQuads(null).isEmpty(), "Boundary face should be culled against its neighbor");
        BakedQuad marker = section.getQuads(side).getFirst();
        check(marker.getDirection() == side, "Lighting direction did not rotate");
        int[] data = marker.getVertices();
        near(x, position(data, 0).x, "Rotated X");
        near(z, position(data, 0).z, "Rotated Z");
        check(data[NORMAL] == normal, "Packed normal did not rotate to " + side);
    }

    private static void assignsOneOwnerToBoundaryFace() {
        BakedQuad face = quad(Direction.EAST, new float[][]{
                {1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}
        });
        var size = new BlockPos(2, 1, 1);
        var culled = ModelSplitter.split(List.of(face), size, true).get(Direction.NORTH);
        check(culled.keySet().equals(Set.of(BlockPos.ZERO)), "An internal plane must have exactly one owner");
        check(!culled.get(BlockPos.ZERO).getQuads(Direction.EAST).isEmpty(), "Internal plane should face east");

        var unculled = ModelSplitter.split(List.of(face), size, false).get(Direction.NORTH);
        check(unculled.get(BlockPos.ZERO).getQuads(Direction.EAST).isEmpty(), "Culling was explicitly disabled");
        check(!unculled.get(BlockPos.ZERO).getQuads(null).isEmpty(), "Disabling culling must keep the face");
    }

    private static BakedQuad quad(Direction direction, float[][] vertices) {
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setDirection(direction);
        consumer.setTintIndex(3);
        consumer.setSprite(UnitTextureAtlasSprite.INSTANCE);
        consumer.setShade(true);
        consumer.setHasAmbientOcclusion(true);
        for (float[] vertex : vertices) {
            float x = vertex[0];
            float y = vertex[1];
            consumer.addVertex(x, y, vertex[2]);
            consumer.setUv(x / 2, y / 2);
            consumer.setColor(Math.round(100 * x), 32, 64, 255);
            consumer.setUv2(Math.round(100 * x), 0);
            if (direction == Direction.DOWN) {
                consumer.setNormal(42 * x / 127, -1, 0);
            } else {
                consumer.setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            }
        }
        return consumer.bakeQuad();
    }

    private static Vector3f position(int[] data, int offset) {
        return new Vector3f(readFloat(data, offset + POSITION), readFloat(data, offset + POSITION + 1),
                readFloat(data, offset + POSITION + 2));
    }

    private static float readFloat(int[] data, int index) {
        return Float.intBitsToFloat(data[index]);
    }

    private static void near(float expected, float actual, String description) {
        check(Math.abs(expected - actual) < 1e-5f, description + " expected " + expected + " but got " + actual);
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
    }
}
