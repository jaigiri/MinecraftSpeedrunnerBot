package net.minecraft.util;

public class Tuple<A, B> {
   private final A a;
   private final B b;

   public Tuple(A aIn, B bIn) {
      this.a = aIn;
      this.b = bIn;
   }

   public A getA() {
      return this.a;
   }

   public B getB() {
      return this.b;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Tuple)) return false;

      Tuple<?, ?> tuple = (Tuple<?, ?>) o;

      if (!a.equals(tuple.a)) return false;
      return b.equals(tuple.b);
   }

   @Override
   public int hashCode() {
      int result = a.hashCode();
      result = 31 * result + b.hashCode();
      return result;
   }
}
