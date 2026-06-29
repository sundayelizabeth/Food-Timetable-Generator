import java.util.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FoodTimetableGenerator {

    record Item(String name, String caf, List<String> slots, int cal, double p, double c, double f) {}

    static List<Item> loadMenu() {
        List<Item> items = new ArrayList<>();
        // Format: {Cafeteria, Name, MealSlots(comma separated), Cal, Protein, Carbs, Fat}
        String[][] data = {
            // Cafeteria A
            {"A","Bread","Breakfast","200","8","40","2"},
            {"A","Boiled Egg","Breakfast","78","6","0.6","5"},
            {"A","Smoothies","Breakfast","150","5","30","2"},
            {"A","Fruits","Breakfast","100","1","25","0.5"},
            {"A","White Rice","Lunch,Dinner","350","8","70","5"},
            {"A","Fried Rice","Lunch,Dinner","400","10","65","12"},
            {"A","Jollof Rice","Lunch,Dinner","380","9","68","10"},
            {"A","Amala","Lunch,Dinner","250","3","55","2"},
            {"A","Vegetable Soup","Lunch,Dinner","200","12","15","15"},
            {"A","Egusi Soup","Lunch,Dinner","220","15","10","20"},
            {"A","Roasted Chicken","Lunch,Dinner","350","30","10","20"},
            {"A","Fried Fish","Lunch,Dinner","300","25","5","20"},
            {"A","Beef","Lunch,Dinner","250","26","0","18"},
            {"A","Moin Moin","Lunch,Dinner","200","15","20","8"},
            {"A","Salad (Veggie)","Lunch,Dinner","100","2","20","2"},
            // Cafeteria B
            {"B","Yam & Egg Sauce","Breakfast","300","12","40","15"},
            {"B","Boiled Egg","Breakfast","78","6","0.6","5"},
            {"B","Puff Puff","Breakfast","250","4","35","10"},
            {"B","White Rice","Lunch,Dinner","350","8","70","5"},
            {"B","Jollof Rice","Lunch,Dinner","380","9","68","10"},
            {"B","White Beans","Lunch,Dinner","250","15","45","5"},
            {"B","Crunchy Chicken","Lunch,Dinner","380","30","15","25"},
            {"B","Fish","Lunch,Dinner","280","25","5","18"},
            {"B","Beef","Lunch,Dinner","250","26","0","18"},
            {"B","Eba","Lunch,Dinner","250","3","55","2"},
            {"B","Fried Plantain","Lunch,Dinner","250","2","40","10"},
            {"B","Vegetable Soup","Lunch,Dinner","200","12","15","15"},
            // Cafeteria C
            {"C","Bread","Breakfast","200","8","40","2"},
            {"C","Boiled Egg","Breakfast","78","6","0.6","5"},
            {"C","White Rice","Lunch,Dinner","350","8","70","5"},
            {"C","Jollof Rice","Lunch,Dinner","380","9","68","10"},
            {"C","Chicken","Lunch,Dinner","350","30","10","20"},
            {"C","Fried Plantain","Lunch,Dinner","250","2","40","10"},
            {"C","Noodles","Lunch,Dinner","300","8","50","10"},
            {"C","Spring Rolls","Lunch,Dinner","180","5","25","8"}
        };
        for (var r : data) {
            items.add(new Item(r[1], r[0], Arrays.asList(r[2].split(",")),
                               Integer.parseInt(r[3]), Double.parseDouble(r[4]),
                               Double.parseDouble(r[5]), Double.parseDouble(r[6])));
        }
        return items;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Item> menu = loadMenu();
        Random rand = new Random();

        System.out.println("===========================================");
        System.out.println("   WELCOME TO MEAL TIMETABLE GENERATOR");
        System.out.println("===========================================");

        System.out.print("\nDo you want a [Fitness] or [Balanced] diet timetable? ");
        boolean fitness = sc.nextLine().trim().toLowerCase().startsWith("f");
        double weight = 70, workoutHour = -1;
        String goal = "";

        if (fitness) {
            System.out.print("Goal (build muscle / lose fat): ");
            goal = sc.nextLine().trim().toLowerCase();
            System.out.print("Body weight (kg): ");
            weight = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Workout time (HH:MM): ");
            try { workoutHour = LocalTime.parse(sc.nextLine().trim(), DateTimeFormatter.ofPattern("HH:mm")).getHour(); }
            catch (Exception e) { workoutHour = 18; }
        }

        // Daily targets
        double cal = fitness ? weight * (goal.contains("muscle") ? 30 : 22) : 2000;
        double protein = fitness ? weight * (goal.contains("muscle") ? 2.0 : 1.6) : 80;
        double carbs = fitness ? cal * (goal.contains("muscle") ? 0.45 : 0.35) / 4 : 250;
        double fat = fitness ? cal * (goal.contains("muscle") ? 0.25 : 0.30) / 9 : 70;

        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        String[] meals = {"Breakfast","Lunch","Dinner"};

        System.out.println("\n===========================================");
        System.out.println("  YOUR " + (fitness ? "FITNESS" : "BALANCED") + " TIMETABLE");
        if (fitness) System.out.println("  Goal: " + goal + " | Weight: " + weight + "kg");
        System.out.println("===========================================\n");

        for (String day : days) {
            System.out.println("--- " + day + " ---");
            double remC = cal, remP = protein, remCb = carbs, remF = fat;
            String lastCaf = "";

            for (String meal : meals) {
                // Build available list for this meal, avoiding last cafeteria if possible
                List<Item> avail = new ArrayList<>();
                for (Item it : menu) {
                    if (it.slots().contains(meal) && !it.caf().equals(lastCaf)) avail.add(it);
                }
                if (avail.isEmpty()) { // fallback: allow any cafeteria
                    for (Item it : menu) if (it.slots().contains(meal)) avail.add(it);
                }
                Collections.shuffle(avail, rand);

                // Pick best item: score = protein*2 - fat*0.5 + carbs*0.1
                boolean postWorkout = fitness && (meal.equals("Lunch") && workoutHour <= 13 || meal.equals("Dinner") && workoutHour >= 16);
                avail.sort((a,b) -> {
                    double sa = a.p()*2 - a.f()*0.5 + a.c()*0.1 + (postWorkout ? a.p()*3 : 0);
                    double sb = b.p()*2 - b.f()*0.5 + b.c()*0.1 + (postWorkout ? b.p()*3 : 0);
                    return Double.compare(sb, sa);
                });

                List<Item> chosen = new ArrayList<>();
                if (!avail.isEmpty()) {
                    chosen.add(avail.get(0));
                    remP -= avail.get(0).p(); remC -= avail.get(0).cal();
                    remCb -= avail.get(0).c(); remF -= avail.get(0).f();
                    // Try to add a second item if first is carb-heavy and a protein side exists
                    if (avail.size() > 1 && avail.get(0).c() > 40) {
                        for (int i = 1; i < Math.min(avail.size(), 4); i++) {
                            if (avail.get(i).p() > 10 && !avail.get(i).caf().equals(avail.get(0).caf())) {
                                chosen.add(avail.get(i));
                                remP -= avail.get(i).p(); remC -= avail.get(i).cal();
                                remCb -= avail.get(i).c(); remF -= avail.get(i).f();
                                break;
                            }
                        }
                    }
                }

                // Print meal
                System.out.print("  " + meal + ": ");
                if (chosen.isEmpty()) { System.out.println("(none)");
                } else {
                    int tc = chosen.stream().mapToInt(i -> i.cal()).sum();
                    double tp = chosen.stream().mapToDouble(i -> i.p()).sum();
                    double tCb = chosen.stream().mapToDouble(i -> i.c()).sum();
                    double tF = chosen.stream().mapToDouble(i -> i.f()).sum();
                    for (int i = 0; i < chosen.size(); i++) {
                        System.out.print(chosen.get(i).name() + " [" + chosen.get(i).caf() + "]");
                        if (i < chosen.size()-1) System.out.print(" + ");
                        lastCaf = chosen.get(i).caf();
                    }
                    System.out.printf(" (~%dkcal, P:%.1fg, C:%.1fg, F:%.1fg)\n", tc, tp, tCb, tF);
                }
            }
            System.out.println();
        }
        System.out.println("===========================================");
        System.out.println("  Done! Enjoy your meals.");
        System.out.println("===========================================");
        sc.close();
    }
}