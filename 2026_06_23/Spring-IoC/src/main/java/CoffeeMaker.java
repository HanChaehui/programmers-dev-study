class CoffeeMaker {

    private Bean bean;

    CoffeeMaker(Bean bean) {
        this.bean = bean;
    }

    void brew() {
        System.out.println(bean.name() + "로 커피를 내립니다");
    }

}
