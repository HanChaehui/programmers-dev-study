public class Pet {

    private String name;
    private int fullness;
    private int happiness;

    public Pet(String name){
        this.name = name;
        fullness = 50;
        happiness = 50;
    }

    public void showStatus(){
        System.out.println("[" + name + "] 포만감: " + fullness + " / 행복: " + happiness);
    }

    public void feed(){
        fullness += 20;
        happiness += 5;
        if(fullness > 100){
            fullness = 100;
        }
        if(happiness > 100){
            happiness = 100;
        }
        System.out.println(name + "에게 먹이를 줬어요! 냠냠");
    }

    public void play(){
        fullness -= 10;
        happiness += 20;
        if(fullness < 0){
            fullness = 0;
        }
        if(happiness > 100){
            happiness = 100;
        }
        System.out.println(name + "와(과) 신나게 놀았어요!");
    }
}
