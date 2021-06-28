package eu.koboo.endpoint.networkable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class NetworkableEncoder {

    private final Map<Integer, Supplier<? extends Networkable>> supplierMap = new ConcurrentHashMap<>();

    public NetworkableEncoder() {
    }

    public <Obj extends Networkable> NetworkableEncoder register(int id, Supplier<Obj> supplier) {
        if(supplierMap.containsKey(id)) {
            throw new IllegalArgumentException("Id '" + id + "' in NetworkSerializer is already used.");
        }
        supplierMap.put(id, supplier);
        return this;
    }

    public <Obj extends Networkable> byte[] encode(Obj networkable) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(buffer);
            int id = getId(networkable);
            if(id == Integer.MIN_VALUE) {
                throw new NullPointerException("No supplier found of classPath '" + networkable.getClass().getName() + "'");
            }
            outputStream.writeInt(id);
            networkable.writeStream(outputStream);
            buffer.close();
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("all")
    public <Obj extends Networkable> Obj decode(byte[] bytes) {
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
            DataInputStream inputStream = new DataInputStream(buffer);
            int id = inputStream.readInt();
            Supplier<? extends Networkable> supplier = getSupplier(id);
            if(supplier == null) {
                throw new NullPointerException("No supplier found of id '" + id + "'");
            }
            Networkable networkable = supplier.get();
            networkable.readStream(inputStream);
            return (Obj) networkable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <Obj extends Networkable> int getId(Obj object) {
        for(Map.Entry<Integer, Supplier<? extends Networkable>> entry : supplierMap.entrySet()) {
            if(entry.getValue().get().getClass().getName().equalsIgnoreCase(object.getClass().getName())) {
                return entry.getKey();
            }
        }
        return Integer.MIN_VALUE;
    }

    public <Obj extends Networkable> Supplier<Obj> getSupplier(int id) {
        return (Supplier<Obj>) supplierMap.get(id);
    }

}
