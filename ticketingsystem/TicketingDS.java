package ticketingsystem;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


public class TicketingDS implements TicketingSystem {



    //默认参数
    private int routenum = 5;
    private int coachnum = 8;
    private int seatnum = 100;
    private int stationnum = 10;
    private int threadnum = 16;
    //原子变量，保证tid唯一
    private AtomicLong Tid = new AtomicLong(0);

    private volatile Seat[] seatList;


    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.stationnum = stationnum;
        this.threadnum = threadnum;
        this.seatList = new Seat[routenum * coachnum * seatnum];
        for (int i = 0; i < routenum; i++) {
            for (int j = 0; j < coachnum; j++) {
                for (int k = 0; k < seatnum; k++) {
                    seatList[i * coachnum * seatnum + j * seatnum + k] = new Seat(i + 1, j + 1, k + 1, stationnum);

                }
            }
        }

    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (route <= routenum && departure > 0 && departure < arrival && arrival <= stationnum) {
            Random rand = new Random();
            Ticket t = new Ticket();
            boolean buysuccess = false;
            boolean randomsucess = false;
            //尝试一次随机购票
            for (int i = 0; i < 1; i++) {
                int coachran = rand.nextInt(coachnum);
                int seatrran = rand.nextInt(seatnum);
                Seat tempSeat = this.seatList[((route - 1)  * coachnum * seatnum) + (coachran * seatnum) + seatrran];

                if (tempSeat.occupySeat(departure, arrival)) {
                    t.passenger = passenger;
                    t.route = route;
                    t.coach = coachran + 1;
                    t.seat = seatrran + 1;
                    t.departure = departure;
                    t.arrival = arrival;
                    t.tid = Tid.getAndIncrement();
                    buysuccess = true;
                    randomsucess = true;
                    tempSeat.saveticket(t);
                    return t;
                }
            }

            //随机失败，遍历查询购票
            if (!buysuccess && !randomsucess) {
                for (int i = ((route - 1) * coachnum * seatnum); i < ((route) * coachnum * seatnum); i++) {
                    if (this.seatList[i].occupySeat(departure, arrival)) {
                        t.passenger = passenger;
                        t.route = route;
                        t.coach = this.seatList[i].getCoachId();
                        t.seat = this.seatList[i].getSeatId();
                        t.departure = departure;
                        t.arrival = arrival;
                        t.tid = Tid.getAndIncrement();
                        buysuccess = true;
                        this.seatList[i].saveticket(t);
                        return t;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        int result = 0;
        //合法性检查
        if (route <= routenum && departure > 0 && departure < arrival && arrival <= stationnum) {
            //根据车次限制查找范围
            for (int i = ((route - 1) * coachnum * seatnum); i < ((route) * coachnum * seatnum); i++) {
                if (this.seatList[i].seatavalibale(departure, arrival)) {
                    result++;
                }
            }
        }
        return result;
    }



    @Override
    public boolean refundTicket(Ticket ticket)
    {
        if (ticket != null)
        {
            //计算座位所在数组位置
            int ticIndex = ((ticket.route - 1) * coachnum * seatnum + ((ticket.coach - 1) * seatnum) + ticket.seat) - 1;
            return this.seatList[ticIndex].releaseSeat(ticket);
        }

        return false;
    }

    @Override
    public boolean buyTicketReplay(Ticket ticket) {
        return false;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket) {
        return false;
    }


}
