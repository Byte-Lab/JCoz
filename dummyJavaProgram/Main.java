public class Main {

    public static void main(String[] args) {
        while (true) {
            int i = 0;
            while (i < 1000000) {
                i++;
            }
            for (int j = 0; j < 10000; j++) {
                BasicMaths basicMaths = new BasicMaths(j, 5);
                basicMaths.basicAdd();
                basicMaths.basicMultiply();
            }
            printOutcome();
        }
    }

    private static void printOutcome() {
        System.out.println("Successful iteration ... \n");
    }

    private static class BasicMaths {
        private int x;
        private int y;
        private int result;

        public BasicMaths(int x, int y) {
            this.x = x;
            this.y = y;
            this.result = 0;
        }

        public void basicAdd() {
            int a = 10;
            int b = 20;
            int c = 30;
            a += b;
            b += c;
            a += b;
            this.result += (this.x + this.y);
        }

        public void basicMultiply() {
            this.result *= (this.x * this.y);
        }

    }

}