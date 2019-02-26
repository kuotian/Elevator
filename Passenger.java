package cn.xdu.elevator;

import java.util.Random;

public class Passenger {
	private static int uniqueId = 0;
	private static int finishCount = 0; //共有多少人完成全部乘梯过程
	
	private static final int MAX_FLOOR = 40;	//最高楼层 
	private Building building;      //所在大楼
	private int id;                 //乘客id
	private int currentFloor;       //所在当前楼层 以0计数
	private int destFloor;          //目的楼层
	private int randomStopSeconds;  //随机停留时间(10-120)
	
	private int timesTakeElevatorL; //可以乘坐的次数(1-10)
	private int countTakeElevatorL; //当前乘坐的次数
	
	private int time2TakeOrLeaveT; //上下电梯时间(命令行输入)
	
	private int time2Requst;    //请求时间(M*60) 时刻

	private boolean finishState;    //是否已经完成全部乘梯过程
	private int state = 0; //乘客状态   0在外面未进入,1在某楼层闲逛,2发起乘梯请求,3在电梯里,4完成所有乘梯活动
	
	private Random random = new Random(); 
	
	
	
	public Passenger(int T,int M, Building building) {
		Passenger.uniqueId++;
		this.id = Passenger.uniqueId;
		this.building = building;
		this.currentFloor = 0;
		//目标楼层在       发起订单后设定
		//随机停留时间  发起订单后设定
		
		this.timesTakeElevatorL = random.nextInt(10)+1;
//		this.timesTakeElevatorL = random.nextInt(3)+1;  //临时测试用
		this.countTakeElevatorL = 0;
		
		this.time2TakeOrLeaveT = T;
		this.time2Requst = random.nextInt(M*60)+1;
//		this.time2Requst = random.nextInt(5)+1;   //临时测试用
		
		this.finishState = false;
		this.state = 0;
	}
	
	
	public static int getFinishCount() {
		return finishCount;
	}


	/**
	 * 以秒计数的动作
	 */
	public void run() {
		if(this.state == 0 && World.WORLD_TIME == this.time2Requst) { //从大厦进来按电梯
			requestTakeElevator();	//发起乘梯请求
			this.state = 2;         //正在等电梯
		}else if(this.state == 1 && World.WORLD_TIME == this.time2Requst) {
			requestTakeElevator();	//发起乘梯请求
			this.state = 2;         //正在等电梯
			building.deleteStayPerson(this, this.currentFloor);
		}else if(this.state == 4) {
			
		}
	}
	
	
	/**
	 * 设置乘梯订单
	 */
	public void setOrder() {
		//更新当前楼层
		this.currentFloor = this.countTakeElevatorL == 0? 0: this.destFloor;//第一次必然在一楼
		//更新目的楼层
		generateDestFloor();
		
		generateRandomStopSeconds();
		
	}
	
	/**
	 * 随机生成目标楼层，且不同于当前楼层
	 * @param destFloor
	 */
	public void generateDestFloor() {
		if(this.countTakeElevatorL == this.timesTakeElevatorL) { //如果是最后一次，目的一定是1楼
			this.destFloor = 0;
		}else {
			//TODO 目前中途不设置1楼为目的地
			this.destFloor = random.nextInt(MAX_FLOOR-1)+1;		
			while(this.destFloor == this.currentFloor) {
				this.destFloor = random.nextInt(MAX_FLOOR-1)+1;		
			}
		}
	}
	/**
	 * 随机生成 该层停留时间
	 */
	public void generateRandomStopSeconds() {
		this.randomStopSeconds = random.nextInt(111)+10;
//		this.randomStopSeconds = random.nextInt(10)+5;   //临时测试用
	}

	
	/**
	 * 获得该层随机停留时间
	 * @return
	 */
	public int getRandomStopSeconds() {
		return randomStopSeconds;
	}

	
	/**
	 * 发起乘梯请求
	 * @param floor
	 */
	public void requestTakeElevator() {
		setOrder();
		if(this.destFloor > this.currentFloor) { //上行
			building.addUpPassenger(this.currentFloor, this);
//			building.activeUpArray(this.currentFloor);
		}else {								     //下行
			building.addDownPassenger(this.currentFloor, this);
//			building.activeDownArray(this.currentFloor);
		}
	}

	
	/**
	 * 获取 乘客id
	 * @return 乘客id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 获取乘客目标楼层
	 * @return 乘客目标楼层
	 */
	public int getDestFloor() {
		return destFloor;
	}

	/**
	 * 获取当前乘梯次数
	 * @return 当前乘梯次数
	 */
	public int getCountTakeElevatorL() {
		return countTakeElevatorL;
	}

	/**
	 * 获取总的乘梯次数
	 * @return 总的乘梯次数
	 */
	public int getTimesTakeElevatorL() {
		return timesTakeElevatorL;
	}

	/**
	 * 上下电梯的时间
	 * @return
	 */
	public int getTime2TakeOrLeaveT() {
		return time2TakeOrLeaveT;
	}

	/**
	 * 下电梯了
	 */
	public void leaveElevator() {
		this.currentFloor = this.destFloor;
		countTakeElevatorL++;
		if(countTakeElevatorL == timesTakeElevatorL+1) {	//完成所有乘梯活动
			finishState = true;
			Passenger.finishCount++;
			this.state = 4;
		}else {
			setTime2Requst(World.WORLD_TIME+getRandomStopSeconds()); //设置下一次请求时刻=当前时刻+该层停留时间
//			System.out.println("下次请求时间"+this.time2Requst);
			this.state = 1;
		}
	}
	
	/**
	 * 设置下一次请求时间
	 * @param time2Requst
	 */
	public void setTime2Requst(int time2Requst) {
		this.time2Requst = time2Requst;
	}

	/**
	 * 上电梯了
	 */
	public void takeElevator() {
		this.state = 3;
		
	}
	
	public void showData() {
		System.out.print("[id:"+id+" orig2dest:"+(this.currentFloor+1)+"-"+(this.destFloor+1)+" 次数:"+countTakeElevatorL+"/"+(timesTakeElevatorL+1)+"] ");
	}


	/**
	 * 是否完成
	 * @return
	 */
	public boolean isFinishState() {
		return finishState;
	}

	
	
}
