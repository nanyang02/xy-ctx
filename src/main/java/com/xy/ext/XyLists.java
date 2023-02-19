package com.xy.ext;

import java.util.*;

/**
 * Class <code>Lists</code>
 *
 * @author yangnan 2023/2/17 10:18
 * @since 1.8
 */
public class XyLists {

    public static class Kv<K, V> {
        private K k;
        private V v;

        public K getK() {
            return k;
        }

        public void setK(K k) {
            this.k = k;
        }

        public V getV() {
            return v;
        }

        public void setV(V v) {
            this.v = v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Kv<?, ?> kv = (Kv<?, ?>) o;
            return Objects.equals(k, kv.k) &&
                    Objects.equals(v, kv.v);
        }

        @Override
        public int hashCode() {
            return Objects.hash(k, v);
        }

        @Override
        public String toString() {
            return "Kv{" +
                    "k=" + k +
                    ", v=" + v +
                    '}';
        }
    }

    public static <K, V> Map<K, Kv<K, V>> getMappingKvMap(Maths.Parity kParity, Maths.Parity vParity, List<Object> dataList) {
        Map<K, Kv<K, V>> map = new HashMap<>();
        Kv<K, V> kv = new Kv<>();
        boolean bk = kParity.match(Maths.Parity.ODD);
        boolean bv = vParity.match(Maths.Parity.EVEN);
        for (int i = 0; i < dataList.size(); i++) {
            Object v = dataList.get(i);
            if (i % 2 == 0) {
                if (bk) kv.setK(XyObjects.cast(v));
                if (!bv) kv.setV(XyObjects.cast(v));
            } else {
                if (!bk) kv.setK(XyObjects.cast(v));
                if (bv) kv.setV(XyObjects.cast(v));
                map.put(kv.getK(), kv);
                kv = new Kv<>();
            }
        }
        return map;
    }

    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>();
    }

}
