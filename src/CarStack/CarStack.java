package CarStack;
public class CarStack {
    int max = 5;
    String[] stack;
    int top;

    public CarStack() {
        stack = new String[max];
        top = -1;
    }


    public void push(String carName) {
        if (carName == null || carName.trim().isEmpty()) {
            return;
        }
        if (top == max - 1) {
            for (int i = 0; i <max- 1; i++) {
                stack[i] = stack[i + 1];
            }
            top--;
        }
        stack[++top] = carName;
    }

    public void display() {
        if (top == -1) {
            System.out.println("No recently viewed cars.");
            return;
        }

        System.out.println("\n=== Recently Viewed Cars ===");
        for (int i = top; i >= 0; i--) {
            System.out.println((top - i + 1) + ". " + stack[i]);
        }
    }
}