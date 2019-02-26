package cn.xdu.elevator;

import java.util.LinkedList;
import java.util.Scanner;

public class World {
	public static int WORLD_TIME = 0; //世界时间
	
	public static void main(String[] args) {
//		int K = 18;//
//		int S = 2; //电梯S秒/层
//		int T = 3; //上下电梯的时间
//		
//		int N = 100; //乘客数 0 1000
//		int M = 1; //入场时间 0 10
		int K, S, T, N ,M;
		K = inputNum(10, 18, "请输入电梯最大载客量[10-18]：");
		S = inputNum(1, 5, "请输入电梯运行速度S秒/层[1-5]：");
		T = inputNum(2, 10, "请输入上下电梯的时间[2,10]秒：");
		N = inputNum(1, 999, "请输入仿真人数 [1,999]：");
		M = inputNum(1, 10, "请输入入场随机时间最大值[1,10]分钟：");
		
		//各种new
		Building building = new Building();
		LinkedList<Passenger> ps = new LinkedList<Passenger>();
		for(int i = 0; i < N; i++) {
			ps.add(new Passenger(T, M, building));
		}
		LinkedList<Elevator> es = new  LinkedList<Elevator>();
		for(int i = 0; i < 10; i++) {
			es.add(new Elevator("E"+i, (int)i/2+1, K, S, building));
		}
		
		
		while(Passenger.getFinishCount() < N) {
			showWorldTime();
			for(int i = 0; i < N; i++) {
				ps.get(i).run();
			}
			for(int i = 0; i < 10; i++) {
				es.get(i).run();
			}
			
			for(int i = 0; i < 10; i++) {
				es.get(i).showData();
			}
			building.showData();
			System.out.println("已完成人数:"+Passenger.getFinishCount()+"/总人数:"+N);
			World.WORLD_TIME++;
		}
		System.out.println("---------仿真结束-----------");
		System.out.println("已完成人数:"+Passenger.getFinishCount()+"/总人数:"+N);
		for(int i = 0; i < 10; i++) {
			es.get(i).showSumData();
		}
	}
	
	public static int inputNum(int min, int max, String inputStr) {
		int num = 0;
		Scanner in = new Scanner(System.in); 
		while(num< min || num > max) {
			System.out.println(inputStr); 
			num = in.nextInt(); 
	        if(num < min || num > max) {
	        	System.out.println("格式错误！");
	        }
		}
		return num;
	}
	
	public static void showWorldTime() {
		System.out.println(World.WORLD_TIME+"S:");
	}
	
	
}
