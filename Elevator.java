package cn.xdu.elevator;

import java.util.LinkedList;
import java.util.Random;

public class Elevator {
	private static final int SUM_FLOOR = 40;    //共40层
	private static final int MAX_FLOOR = 39;	//最高楼层编号为39
	private static final int MIN_FLOOR = 0;     //最低楼层编号为0
	private Building building;                  //所在大楼
	
	private String name;			   //电梯编号名字E0 E1...
	private boolean[] availableFloor;  //可达楼层 
	private int currentFloor;          //当前楼层 从0开始
//	private int destFloor;             //目的楼层  -1代表没有目标楼层
	private boolean[] offElevatorRequestList;  //电梯里的人下电梯的请求
	private int maxCapacityK;          //最大载客量K
	private int speedS;                //运行速度 S秒/层
	private boolean directionUp;       //运行方向 true向上
	private int runState;              //0是空闲 1运行 2停在某一层
	private int timeRun;               //运行时间
	private int timeStop;              //空闲时间
	private int currentPessengerCount; //当前乘客数
	private int time2NextFloor;       //到达下一楼层的时间
	private int countTime2TakeOrLeave;//计时上下电梯的时间
	
	private LinkedList<Passenger> currentPassengerList; //电梯上的乘客
	private Random random = new Random(); 
	
	
	
	public Elevator(String name, int mode,int K, int S,Building building) {
		this.building = building;
		this.name = name;
		this.availableFloor = new boolean[SUM_FLOOR];
		setAvailableFloorMode(mode);
		
		this.currentFloor = random.nextInt(SUM_FLOOR);	
//		this.destFloor = -1;
		this.offElevatorRequestList = new boolean[SUM_FLOOR];
		for(int i = 0; i < SUM_FLOOR; i++) {
			this.offElevatorRequestList[i] = false;
		}
		this.maxCapacityK = K;
		this.speedS = S;
		this.directionUp = true;
		this.runState = 0;
		this.timeRun = 0;
		this.timeStop = 0;
		this.currentPessengerCount = 0;
		this.time2NextFloor = S;   //?
		this.countTime2TakeOrLeave = 0;
		this.currentPassengerList = new LinkedList<Passenger>();
	}
	
	/**
	 * 以秒计数的动作
	 */
	public void run() {
		if(runState == 0) {	//空闲状态 
			this.timeStop++;
			checkNextStep();
		}else if(runState == 1){ //正在运行
			this.timeRun++;
			this.time2NextFloor--;
			if(this.time2NextFloor == 0) { //到达某一层
				this.time2NextFloor = this.speedS;
				this.currentFloor = this.directionUp == true? (this.currentFloor+1):(this.currentFloor-1); //更新楼层
				
				if(this.currentFloor == MIN_FLOOR) {
					this.directionUp = true;
				}else if(this.currentFloor == MAX_FLOOR) {
					this.directionUp = false;
				}
				if(currentPessengerCount == 0) { //空梯 
					checkNextStep();
				}
				if(checkLeave() || checkTake(directionUp)) {	//有人要下电梯 或 有人要上电梯
					this.runState = 2;
				}
			}
		}else if(runState == 2) {  //停在某一楼层，开关门，上下客
			this.timeRun++;
			//先下电梯 后上电梯
			Passenger p = leaveElevator();
			if(p != null) {	//有乘客要下电梯
				this.countTime2TakeOrLeave++;	//模拟乘客正在下电梯
				if(this.countTime2TakeOrLeave == p.getTime2TakeOrLeaveT()) {
					this.countTime2TakeOrLeave = 0;
					currentPassengerList.remove(p); //真的下电梯了   //???不知道能不能真的remove 一会儿检查 
					this.currentPessengerCount--;
					building.addStayPerson(this.currentFloor, p);
					p.leaveElevator();
				}
			}else { //没有人下电梯，考虑同方向上电梯
				offElevatorRequestList[this.currentFloor] = false;
				p = building.getPassengerOneTakeElevator(this.currentFloor, availableFloor,this.directionUp);
				if(!isFull() && p != null) {	//电梯未满 且有乘客要进入电梯
					this.countTime2TakeOrLeave++;	//模拟乘客正在上电梯
					if(this.countTime2TakeOrLeave == p.getTime2TakeOrLeaveT()) {
						this.countTime2TakeOrLeave = 0;
						this.currentPessengerCount++;
						currentPassengerList.add(p);
						offElevatorRequestList[p.getDestFloor()] = true;
						building.deletePassenger(p, this.currentFloor, directionUp);
						p.takeElevator();
						
					}
				}else { //也没人出电梯，电梯也不用进人了
					if(currentPessengerCount == 0) { //空梯 
						checkNextStep();
					}else {
						this.runState = 1;
					}
					
				}
			}
			
		}
	}
	
	/**
	 * 获得要下电梯的一位乘客
	 * @return 乘客 
	 */
	public Passenger leaveElevator() {
		for (Passenger cp : currentPassengerList) {
			if(cp.getDestFloor() == this.currentFloor) {
				return cp;
			}
		}
		//这层乘客下完了
		offElevatorRequestList[this.currentFloor] = false;
		return null;
	}
	
	
	
	/**
	 * 检查是否有人要下电梯
	 * @return true 有
	 */
	public boolean checkLeave() {
		return offElevatorRequestList[this.currentFloor];
	}
	
	/**
	 * 检查是否有人要上电梯
	 * @param directionUp 上行true 下行false
	 * @return true是 false否
	 */
	public boolean checkTake(boolean directionUp) {
		if(directionUp) {	//上行
			return building.checkUpTake(this.currentFloor, availableFloor);
		}else {	//下行
			return building.checkDownTake(this.currentFloor, availableFloor);
		}
	}
	
	/**
	 * 确定接下来电梯运行状态
	 */
	public void checkNextStep() {
		if(directionUp) {
			if(checkTake(true)) {	//如果当前层有人要上
				this.runState = 2;	//直接开门
				return;
			}
			for(int i = this.currentFloor+1; i <= MAX_FLOOR; i++) { //如果上层有人请求，先响应
				if(availableFloor[i]) {
					if(building.checkUpTake(i, availableFloor) || building.checkDownTake(i, availableFloor)) {
						this.runState = 1;  //向上运行
						return;
					}
				}
				
			}
			if(checkTake(false)) {	//如果当前层有人要下
				this.directionUp = false; //改变方向
				this.runState = 2;	//直接开门
				return;
			}
			for(int i = this.currentFloor-1; i >= MIN_FLOOR; i--) {//如果下层有人请求
				if(availableFloor[i]) {
					if(building.checkUpTake(i, availableFloor) || building.checkDownTake(i, availableFloor)) {
						this.directionUp = false; //改变方向
						this.runState = 1;  //向下运行
						return;
					}
				}
				
			}
			//没有请求
			runState = 0;
		}else {
			if(checkTake(false)) {	//如果当前层有人要进电梯
				this.runState = 2;	//直接开门
				return;
			}
			for(int i = this.currentFloor-1; i >= MIN_FLOOR; i--) {//如果下层有人请求,先响应
				if(availableFloor[i]) {
					if(building.checkUpTake(i, availableFloor) || building.checkDownTake(i, availableFloor)) {
						this.runState = 1;  //向下运行
						return;
					}
				}
				
			}
			if(checkTake(true)) {	//如果当前层有人要进电梯向上
				this.directionUp = true; //改变方向
				this.runState = 2;	//直接开门
				return;
			}
			for(int i = this.currentFloor+1; i <= MAX_FLOOR; i++) { //如果上层有人请求
				if(availableFloor[i]) {
					if(building.checkUpTake(i, availableFloor) || building.checkDownTake(i, availableFloor)) {
						this.directionUp = true; //改变方向
						this.runState = 1;  //向上运行
						return;
					}
				}
			}
			//没有请求
			runState = 0;
		}
	}
	/**
	 * 判断该层，本电梯是否可达
	 * @param floor
	 * @return
	 */
	public boolean canReachFloor(int floor) {
		return this.availableFloor[floor];
	}
	/**
	 * 是否满员
	 * @return
	 */
	public boolean isFull() {
		return currentPessengerCount >= maxCapacityK;
	}
	
	/**
	 * 设定电梯的可达楼层模式
	 * 模式1:到达每层
	 * 模式2:可到达1,25-40楼 (编号0,24-39)
	 * 模式3:可到达1-25层 (编号0-24)
	 * 模式4:可到达1、2~40层中偶数层(编号0,1-39奇数层)
	 * 模式5:可到达1~39层中奇数层(编号0-39偶数层)
	 * @param mode 选择模式
	 */
	public void setAvailableFloorMode(int mode) {
		for(int i = 0; i < SUM_FLOOR; i++) {
			this.availableFloor[i] = false;
		}
		switch(mode) {
		case 1:setAllAvailableFloorMode1();break;
		case 2:setAllAvailableFloorMode2();break;
		case 3:setAllAvailableFloorMode3();break;
		case 4:setAllAvailableFloorMode4();break;
		case 5:setAllAvailableFloorMode5();break;
		}
	}
	/**
	 * 模式1:到达每层
	 */
	public void setAllAvailableFloorMode1() {
		for(int i = 0; i < SUM_FLOOR; i++) {
			this.availableFloor[i] = true;
		}
	}
	/**
	 * 模式2:可到达1,25-40楼(编号0,24-39)
	 */
	public void setAllAvailableFloorMode2() {
		this.availableFloor[0] = true;
		for(int i = 24; i <= MAX_FLOOR; i++) {
			this.availableFloor[i] = true;
		}
	}
	/**
	 * 模式3:可到达1-25层 (编号0-24)
	 */
	public void setAllAvailableFloorMode3() {
		for(int i = 0; i <= 24; i++) {
			this.availableFloor[i] = true;
		}
	}
	/**
	 * 模式4:可到达1、2~40层中偶数层(编号0,1-39奇数层)
	 */
	public void setAllAvailableFloorMode4() {
		this.availableFloor[0] = true;
		for(int i = 1; i <= MAX_FLOOR; i+=2) {
			this.availableFloor[i] = true;
		}
	}
	/**
	 * 模式5:可到达1~39层中奇数层(编号0-39偶数层)
	 */
	public void setAllAvailableFloorMode5() {
		for(int i = 0; i < MAX_FLOOR; i+=2) {
			this.availableFloor[i] = true;
		}
	}
	
	public void showData() {
		System.out.print("电梯"+name+"在第"+(this.currentFloor+1)+"层.方向:");
		if(directionUp) {
			System.out.print("上");
		}else {
			System.out.print("下");
		}
		System.out.print("状态:");
		if(runState == 0) {
			System.out.print("空闲");
		}else if(runState == 1) {
			System.out.print("运行");
		}else {
			System.out.print("开门ing-乘客正在上下电梯");
		}
		System.out.print(" .乘客数:"+this.currentPessengerCount+".");
		for (Passenger p : currentPassengerList) {
			p.showData();
		}
		System.out.println();
	}
	
	public void showSumData() {
		System.out.println("电梯"+name+"空闲时间:"+this.timeStop+"  运行时间:"+this.timeRun);
	}
}
