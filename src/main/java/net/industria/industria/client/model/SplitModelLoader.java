package net.industria.industria.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.obj.ObjLoader;

public final class SplitModelLoader implements IGeometryLoader<SplitModelGeometry> {
    @Override
    public SplitModelGeometry read(JsonObject json, JsonDeserializationContext context) {
        JsonArray size = GsonHelper.getAsJsonArray(json, "size");
        if (size.size() != 3) {
            throw new JsonParseException("Split model size must have three entries [x, y, z]");
        }
        BlockPos dimensions = new BlockPos(readDimension(size.get(0)), readDimension(size.get(1)),
                readDimension(size.get(2)));
        return new SplitModelGeometry(ObjLoader.INSTANCE.read(json, context), dimensions);
    }

    private static int readDimension(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            try {
                int size = element.getAsBigDecimal().intValueExact();
                if (size > 0) {
                    return size;
                }
            } catch (ArithmeticException | NumberFormatException ignored) {
                // Report the same JSON error for all invalid dimensions.
            }
        }
        throw new JsonParseException("Split model dimensions must be positive integers");
    }
}
