import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

class Customer
{
    int id;//序号
    double x;//
    double y;//位置
}
class Solution //解类
{
    ArrayList<Integer> route = new ArrayList<>();//列表存放解

    double route_length;//路径长度
    double getRoute_length()//获取路径长度
    {
        double route_length = 0;
        int cur_i = route.get(0);
        int i = 1;
        while(cur_i != route.get(route.size()-1))//循环直到最后一个
        {
            route_length += TSP.cost_mariax[cur_i][route.get(i)];
            cur_i = route.get(i);
            i++;
        }
        route_length += TSP.cost_mariax[route.get(0)][cur_i];//加回起点
        return route_length;
    }
    void shuffeRoute()//随机生成一个Route
    {
        for(int i=1;i<=TSP.CUSTOMER_NUMBER;i++)
        {
            this.route.add(i);
        }
        Collections.shuffle(this.route);//随机化序列
        this.route_length = getRoute_length();
    }
    void print()
    {
        System.out.println("该解的路线为");
        for(int i=0;i<=TSP.CUSTOMER_NUMBER-1;i++)
        {
            System.out.print(this.route.get(i));

            if(i==TSP.CUSTOMER_NUMBER-1)
            {
                System.out.print("-");
                System.out.print(this.route.get(0));
                continue;
            }
            System.out.print("-");
        }
        System.out.println("该解总长度为"+this.route_length);
    }
    public Solution clone()
    {
        Solution solution = new Solution();
        solution.route.addAll(this.route);
        solution.route_length = this.route_length;
        return solution;
    }

}
abstract class Strategy//策略类
{
    final  static int iter_number = 4000;//设定局部搜索的次数
    public static Solution two_opt(Solution solution)//两元素交换
    {
        for(int i=1;i<=TSP.CUSTOMER_NUMBER-1;i++)
            for(int j=i+1;j<=TSP.CUSTOMER_NUMBER;j++) {
                Solution new_solution = solution.clone();
                ArrayList<Integer> new_route = new ArrayList<>();
                new_route.addAll(new_solution.route.subList(i, j));
                new_solution.route.removeAll(new_route);
                Collections.reverse(new_route);
                new_solution.route.addAll(i, new_route);
                new_solution.route_length = new_solution.getRoute_length();
                //System.out.println(i);
                //System.out.println(j);
                if(new_solution.route_length<solution.route_length)
                {
                    //new_solution.print();
                    //System.out.println(solution.route.size());
                    return new_solution;
                }
            }


        return solution;
    }


    public static Solution shaking(Solution solution, int k)//shaking函数，获得邻域外的解
    {

            int [] has_exange = new int[TSP.CUSTOMER_NUMBER+1];//储存交换过的解
            for(int i=1;i<=k;i++)//通过k次2-opt获得k重邻域
            {
                Random r = new Random();
                int p = r.nextInt(TSP.CUSTOMER_NUMBER);
                int q = r.nextInt(TSP.CUSTOMER_NUMBER-p)+p+1;
                ArrayList<Integer> new_route = new ArrayList<>();
                new_route.addAll(solution.route.subList(p, q));
                solution.route.removeAll(new_route);
                Collections.reverse(new_route);
                solution.route.addAll(p, new_route);
                solution.route_length = solution.getRoute_length();

            }
            return solution;
    }
    public static Solution local_search(Solution solution)//邻域搜索
    {
        while(true) {
            Solution new_solution = new Solution();
            new_solution.route.addAll(solution.route);
            new_solution.route_length = solution.route_length;

            new_solution = two_opt(new_solution);

            if (solution.route_length == new_solution.route_length)
                break;
            solution = new_solution;
        }
        return solution;

    }
    public static Solution V_N_Search(Solution solution)//变邻域搜索
    {
        int k = 1;
        while(k<=TSP.CUSTOMER_NUMBER)
        {
            Solution new_solution = new Solution();
            new_solution.route.addAll(solution.route);
            new_solution.route_length = solution.route_length;
            new_solution = shaking(new_solution,k);
            new_solution = local_search(new_solution);
            if(solution.route_length>new_solution.route_length)
            {
                solution = new_solution;
                k = 1;
            }
            else k++;


        }
        return solution;
    }
    public static Solution R_V_N_Search(Solution solution)
    {
        int k = 1;
        for(int i=1;i<=700;i++) {
            k=1;
            while (k <= TSP.CUSTOMER_NUMBER) {
                //System.out.println(k);
                Solution new_solution = new Solution();
                new_solution.route.addAll(solution.route);
                new_solution.route_length = solution.route_length;
                long endTime = System.currentTimeMillis();
                new_solution = shaking(new_solution, k);
                long startTime = System.currentTimeMillis();
                if (solution.route_length <= new_solution.route_length) {

                    k++;
                } else {
                    solution = new_solution;
                    k = 1;
                }


            }
        }
        return solution;
    }
}
public class TSP {
    static Customer[] customers;
    static int CUSTOMER_NUMBER;//顾客数
    static double[][] cost_mariax;//距离矩阵
    static double get_distance(Customer a,Customer b)//返回两个顾客间距离
    {
        return Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
    }
    static double[][] get_cost_mariax(double[][] cost_mariax)//获得距离矩阵方便运算
    {
        for(int i = 1;i<=CUSTOMER_NUMBER;i++)
            for(int j = 1;j<=CUSTOMER_NUMBER;j++) {
                cost_mariax[i][j] = get_distance(customers[i], customers[j]);

            }
        return cost_mariax;
    }
    static void input()
    {
        File input = new File("berlin52.tsp");
        try {
            InputStreamReader ir = new InputStreamReader(new FileInputStream(input));
            BufferedReader br = new BufferedReader(ir);
            String line = " ";
            line = br.readLine();
            CUSTOMER_NUMBER = Integer.valueOf(line);
            customers =new Customer[CUSTOMER_NUMBER+1];
            while(true)
            {
                line = br.readLine();
                if(line.equals("End"))break;
                String[] data = line.split(" ");
                Customer cur_customer = new Customer();
                cur_customer.id = Integer.valueOf(data[0]);
                cur_customer.x =  Double.valueOf(data[1]);
                cur_customer.y =  Double.valueOf(data[2]);
                customers[cur_customer.id] = cur_customer;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }

    public static void main(String args[]) {
        input();
        cost_mariax = new double[CUSTOMER_NUMBER + 1][CUSTOMER_NUMBER + 1];
        cost_mariax = get_cost_mariax(cost_mariax);
        double ans = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 20; i++) {
            Solution solution = new Solution();
            solution.shuffeRoute();
            for (int j = 1; j <= 3000; j++) {
                Solution new_solution = solution.clone();
                new_solution = Strategy.two_opt(new_solution);
                if (new_solution.route_length < solution.route_length)
                    solution = new_solution;

                //}
                //   solution.print();
                // solution = Strategy.V_N_Search(solution);
                //   }
            }
            solution.print();
            ans += solution.route_length;

        }
            long endTime = System.currentTimeMillis();
            System.out.println("程序运行时间：" + (endTime - startTime) / 1000 + "s");
            System.out.println(ans / 20.0);

        }

    }
