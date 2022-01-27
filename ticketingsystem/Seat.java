package ticketingsystem;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.*;

public class Seat {
    private int routeId;

    private int coachId;
    private int seatId;
    private int stationnum;
    //区间表
    private int[] stationList;

    private final ReentrantLock lock = new ReentrantLock();
    //已售出票
    private CopyOnWriteArrayList<Ticket> soldtickets = new CopyOnWriteArrayList<Ticket>();

    public Seat(int routeId, int coachId, int seatId, int stationnum) {
        this.routeId = routeId;
        this.coachId = coachId;
        this.seatId = seatId;
        this.stationnum = stationnum;
        stationList = new int[stationnum];
        for (int i = 1; i <= stationnum; i++) {
            stationList[i - 1] = i;
        }
        soldtickets.clear();

    }

    protected boolean occupySeat(int departure, int arrival) {
            lock.lock();

            if (this.seatavalibale(departure, arrival)) {
                for (int i = departure - 1; i < arrival - 1; i++) { //购票区间置0，留尾不留头
                    this.stationList[i] = 0;
                }
                lock.unlock();
                return true;
            } else {
                lock.unlock();
                return false;
            }
    }


    public boolean releaseSeat(Ticket ticket) {
        lock.lock();
        //检查车票有效性
         if ( this.checkticket(ticket) && !this.seatavalibale(ticket.departure, ticket.arrival)) {
             //相对应的区间表车站标号还原
             for (int i = ticket.departure - 1; i < ticket.arrival - 1; i++) {
                    this.stationList[i] = i + 1;
                }
             //维护已经购票列表
                this.removeticket(ticket);
                lock.unlock();
                return true;

            } else {
                lock.unlock();
                return false;
            }
    }



    public boolean seatavalibale(int departure, int arrival) {
            //合法性检查
            if (departure >= arrival || departure < 1 || departure >= stationnum || arrival > stationnum) {
                return false;
            } else {
                for (int i = departure - 1; i < arrival - 1; i++) {
                    if (this.stationList[i] == 0) {//查询区间中出现0说明被占用，立即返回
                        return false;
                    }
                }
            }
            return true;
    }

    protected void saveticket(Ticket t) {  this.soldtickets.add(t);    }

    protected boolean removeticket(Ticket t){ return  this.soldtickets.remove(t);}

    protected boolean checkticket(Ticket tic){
        for (Ticket t:soldtickets){ //检查售出票中是否存在
            if(tic.equals(t)){return true;}
        }
        return false;
    }



    public int getSeatId() {
        return seatId;
    }

    public int getRouteId() {return routeId;}

    public int getCoachId() {return coachId;}
}
