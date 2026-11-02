public class Budget {
    int total = 1000;
    int available = 1000;
    int frozen = 0;
    int used = 0;

    boolean check() {
        if(total==available+frozen+used){
        return true;}
        else{
            return false;
        }
    }

    boolean submit(int amount) {
        if(amount<=0||amount>available){
            return false;
        }
        available=available-amount;
        frozen=frozen+amount;
        return true; 
    }

    public static void main(String[] args) {
        Budget b = new Budget();
        System.out.println(b.available + "/" + b.frozen + "/" + b.used);
        System.out.println("守恒: " + b.check());
    System.out.println("提交 600: " + b.submit(600));
    System.out.println(b.available + "/" + b.frozen + "/" + b.used);
    System.out.println("守恒: " + b.check());
    System.out.println("提交 9999: " + b.submit(9999));
    System.out.println(b.available + "/" + b.frozen + "/" + b.used);
    System.out.println("守恒: " + b.check());
    }
}