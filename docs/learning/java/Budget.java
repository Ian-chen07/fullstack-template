public class Budget {  //创建表单
    int total = 1000;   //各个栏目 总共，可用，冻结，已用
    int available = 1000;
    int frozen = 0;
    int used = 0;

    boolean check() {             //检查符不符合守恒这个是一定要成立的
        if(total==available+frozen+used){
        return true;}
        else{
            return false;
        }
    }

    boolean submit(int amount) {       //学生申请 同时把这笔钱预留出来，别人不能再花。
        if(amount<=0||amount>available){   
            return false;
        }
        available=available-amount; 
        frozen=frozen+amount;
        applying = amount;  //申请金额          
        return true; 
    }
    int applying = 0;   // 这张申请单要多少钱
    int approved = 0;   // 老师批准了多少

boolean approve(int amount) {      //老师审批 给多少留多少
    if (amount <= 0 || amount > applying) {
        return false;              // 批 0 或超过申请额，直接拒绝
    }
    int diff = applying - amount;  // 算差价多预留出来的钱 多出来的返回available
    frozen-=diff;           //同时冻结数减少
    available+=diff;        // diff 从冻结退回可用
    approved=amount;          // 记下老师批了多少 approved一开始是0的所以需要这样记录
    return true;
}

boolean settle(int actual) {  //实际使用后 有余钱 将最后的结果确定并加入已用used中 这一块是财务也就是后台负责
    if (actual < 0 || actual > approved) {
        return false;                 // 花超了，拒绝
    }
    frozen-=approved;             // 冻结减掉 approved
    used+=actual;                // 已使用加上 actual
    available+=approved-actual;       //可用加上 approved - actual
    return true;
}

    public static void main(String[] args) {
        Budget b = new Budget();  //新建表单b
            System.out.println(b.available + "/" + b.frozen + "/" + b.used); //表单栏目
            System.out.println("守恒: " + b.check()); //输出check步骤检查结果
        System.out.println("提交 600: " + b.submit(600)); //测试600申请
        System.out.println(b.available + "/" + b.frozen + "/" + b.used); //显示此时结果
        System.out.println("守恒: " + b.check());
        System.out.println("提交 9999: " + b.submit(9999)); //同上依旧测试
        System.out.println(b.available + "/" + b.frozen + "/" + b.used);
        System.out.println("守恒: " + b.check()); //依旧检查守恒公式
    System.out.println("批准 500: " + b.approve(500));  //批准500 
    System.out.println(b.available + "/" + b.frozen + "/" + b.used); //输出此时的情况
    System.out.println("守恒: " + b.check());  //检查之后不写了 就写一个check都是重复步骤
    System.out.println("结算 460: " + b.settle(460));  //实际花销
    System.out.println(b.available + "/" + b.frozen + "/" + b.used);
    System.out.println("守恒: " + b.check()); //check
    }
}