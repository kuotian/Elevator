package cn.xdu.elevator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Building {
	private static final int SUM_FLOOR = 40;	//楼层总数
	private static final int MAX_FLOOR = 39;	//最高楼层编号为39
	private static final int MIN_FLOOR = 0;     //最低楼层编号为0
	
	Map<Integer, LinkedList<Passenger>> stayPersonMap = new HashMap<Integer,LinkedList<Passenger>>();    //在闲逛，不按电梯的人 key为楼层号
	Map<Integer, LinkedList<Passenger>> upPassengerMap = new HashMap<Integer,LinkedList<Passenger>>();   //请求向上乘梯的人
	Map<Integer, LinkedList<Passenger>> downPassengerMap = new HashMap<Integer,LinkedList<Passenger>>(); //请求向下乘梯的人
	
	public Building() {
		for(int i = 0; i < SUM_FLOOR; i++) {
			stayPersonMap.put(i, new LinkedList<Passenger>());
			upPassengerMap.put(i, new LinkedList<Passenger>());
			downPassengerMap.put(i, new LinkedList<Passenger>());
		}
	}


	
	/**
	 * 增加每层停留的人
	 * @param floor
	 * @param p
	 */
	public void addStayPerson(int floor, Passenger p) {
		LinkedList<Passenger> person = stayPersonMap.get(floor);
		person.add(p);
		stayPersonMap.put(floor, person);
	}
	/**
	 * 删除每层停留人员一个
	 * @param p 指定人
	 * @param floor 楼层号
	 */
	public void deleteStayPerson(Passenger p, int floor) {
		LinkedList<Passenger> person = stayPersonMap.get(floor);
		person.remove(p);
	}
	
	/**
	 * 增加向上请求的乘客信息 
	 * @param floor 楼层
	 * @param p 乘客
	 */
	public void addUpPassenger(int floor, Passenger p) {
		LinkedList<Passenger> passengers = upPassengerMap.get(floor);
		passengers.add(p);
		upPassengerMap.put(floor, passengers);
	}
	/**
	 * 增加向下请求的乘客信息 
	 * @param floor 楼层key
	 * @param p 乘客
	 */
	public void addDownPassenger(int floor, Passenger p) {
		LinkedList<Passenger> passengers = downPassengerMap.get(floor);
		passengers.add(p);
		downPassengerMap.put(floor, passengers);
	}
	
	/**
	 * 返回一个可以上电梯的乘客
	 * @param floor 楼层号
	 * @param availableFloor 电梯可达楼层
	 * @return 乘客
	 */
	public Passenger getPassengerOneTakeElevator(int floor, boolean[] availableFloor, boolean directionUp) {
		LinkedList<Passenger> passengers = null;
		if(directionUp) {
			passengers = upPassengerMap.get(floor);
		}else {
			passengers =downPassengerMap.get(floor);
		}
		
		for (Passenger passenger : passengers) {
			if(availableFloor[passenger.getDestFloor()]) {	//有符合要求的向上请求乘客
				return passenger;
			}
		}
		return null;
	}
	
	/**
	 * 从请求列表删除一个乘客
	 * @param p
	 * @param floor
	 * @param directionUp
	 */
	public void deletePassenger(Passenger p, int floor, boolean directionUp) {
		LinkedList<Passenger> passengers = null;
		if(directionUp) {
			passengers = upPassengerMap.get(floor);
		}else {
			passengers =downPassengerMap.get(floor);
		}
		passengers.remove(p);
	}
	
	/**
	 * 查询该层是否有向上的请求
	 * @param floor 楼层号
	 * @param availableFloor 电梯可达楼层
	 * @return
	 */
	public boolean checkUpTake(int floor, boolean[] availableFloor) {
		LinkedList<Passenger> passengers = upPassengerMap.get(floor);
		for (Passenger passenger : passengers) {
			if(availableFloor[passenger.getDestFloor()]) {	//有符合要求的向上请求乘客
				return true;
			}
		}

		return false;
	}
	/**
	 * 查询该层是否有向下的请求
	 * @param floor 楼层号
	 * @return
	 */
	public boolean checkDownTake(int floor, boolean[] availableFloor) {
		LinkedList<Passenger> passengers = downPassengerMap.get(floor);
		for (Passenger passenger : passengers) {
			if(availableFloor[passenger.getDestFloor()]) {	//有符合要求的向上请求乘客
				return true;
			}
		}
		return false;
	}
	
	
	public void showData() {
		System.out.println("Building:");
		
		
		
		for(int i = 0; i < SUM_FLOOR; i++) {
			System.out.print("楼层:"+(i+1)+"   向上请求乘客列表:");
			LinkedList<Passenger> passengers = upPassengerMap.get(i);
			for (Passenger p : passengers) {
				p.showData();
			}
			System.out.println();
			System.out.print("                      向下请求乘客列表:");
			passengers = downPassengerMap.get(i);
			for (Passenger p : passengers) {
				p.showData();
			}
			System.out.println();
			System.out.print("                      记录在该层闲逛的人员列表:");
			passengers = stayPersonMap.get(i);
			if(i == 0) { //在1层时
				for (Passenger p : passengers) {
					if(!p.isFinishState()) {
						p.showData();
					}
				}
				System.out.println();
				continue;
			}
			for (Passenger p : passengers) {
					p.showData();
			}
			System.out.println();
		}
		System.out.print("已完成人员列表:");
		LinkedList<Passenger> passengers = stayPersonMap.get(0);
		for (Passenger p : passengers) {
			if(p.isFinishState()) {
				p.showData();
			}
		}
		System.out.println();
	}
}
