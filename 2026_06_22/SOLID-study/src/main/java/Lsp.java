public class Lsp {
}

class Bird {
    void eat() {
        System.out.println("냠냠 먹습니다");
    }
}

class FlyingBird extends Bird {
    void fly() {
        System.out.println("훨훨 납니다");
    }
}

class Sparrow extends FlyingBird {
}

class Penguin extends Bird {
    void swim() {
        System.out.println("첨벙 헤엄칩니다");
    }
}