package eu.koboo.endpoint.transferable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TransferCodec {

    private static final TransferCodec TRANSFER_CODEC = new TransferCodec();

    public static TransferCodec getInstance() {
        return TRANSFER_CODEC;
    }

    private final Map<Integer, Supplier<? extends Transferable>> supplierMap = new ConcurrentHashMap<>();

    private TransferCodec() {
    }

    public <Obj extends Transferable> TransferCodec register(int id, Supplier<Obj> supplier) {
        if(supplierMap.containsKey(id)) {
            throw new IllegalArgumentException("Id '" + id + "' in TransferCodec is already used.");
        }
        supplierMap.put(id, supplier);
        return this;
    }

    public <Obj extends Transferable> byte[] encode(Obj transferable) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(buffer);
            int id = getId(transferable);
            if(id == Integer.MIN_VALUE) {
                throw new NullPointerException("No supplier found of classPath '" + transferable.getClass().getName() + "'");
            }
            outputStream.writeInt(id);
            transferable.writeStream(outputStream);
            buffer.close();
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("all")
    public <Obj extends Transferable> Obj decode(byte[] bytes) {
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
            DataInputStream inputStream = new DataInputStream(buffer);
            int id = inputStream.readInt();
            Supplier<? extends Transferable> supplier = getSupplier(id);
            if(supplier == null) {
                throw new NullPointerException("No supplier found of id '" + id + "'");
            }
            Transferable transferable = supplier.get();
            transferable.readStream(inputStream);
            return (Obj) transferable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <Obj extends Transferable> int getId(Obj object) {
        for(Map.Entry<Integer, Supplier<? extends Transferable>> entry : supplierMap.entrySet()) {
            if(entry.getValue().get().getClass().getName().equalsIgnoreCase(object.getClass().getName())) {
                return entry.getKey();
            }
        }
        return Integer.MIN_VALUE;
    }

    public <Obj extends Transferable> Supplier<Obj> getSupplier(int id) {
        return (Supplier<Obj>) supplierMap.get(id);
    }

}
