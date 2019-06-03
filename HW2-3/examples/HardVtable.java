class HardV{
    public static void main(String[] a){
    }
}


class A {
    int i;
    int j;
    public int meth() {
        return 1;
    }
    public int bar() {
        return 2;
    }
    public int AAA() {
        return 10;
    }
}

class B extends A {
    int p;
    public int meth() {
        return 4;
    }
    public boolean aaa() {
        return true;
    }
}

class C extends B {

    public int[] myarrayC(boolean p) {
        return new int[43];
    }
    public int AAA() {
        return 423;
    }
    public int meth() {
        return 1;
    }

    //public int[] lalala(boolean p) {
    //    return !p && p;
    //}
}

class AAA extends A {
    public int barrrrrrr() {
        return new int[4].length;
    }
}