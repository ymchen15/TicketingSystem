package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    final static int routenum = 10;
    final static int coachnum = 10;
    final static int seatnum = 100;
    final static int stationnum = 20;
    final static int testnum = 100000;

    final static int retpc = 10;
    final static int buypc = 30;
    final static int inqpc = 100;
    final static int threadnum = 64;

    final static long[] buyTicketTime = new long[threadnum];
    final static long[] refundTime = new long[threadnum];
    final static long[] inquiryTime = new long[threadnum];

    final static long[] buyTotal = new long[threadnum];
    final static long[] refundTotal = new long[threadnum];
    final static long[] inquiryTotal = new long[threadnum];

    private final static AtomicInteger threadId = new AtomicInteger(0);

    static String passengerName() {
        Random rand = new Random();
        long uid = rand.nextInt(testnum);
        return "passenger" + uid;
    }

    public static void main(String[] args) throws InterruptedException {
        final int[] threadNums = { 4, 8, 16, 32, 64};
        int i;
        for (i = 0; i < threadNums.length; ++i) {
            final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadNums[i]);
            Thread[] threads = new Thread[threadNums[i]];
            for (int j = 0; j < threadNums[i]; j++) {
                threads[j] = new Thread(new Runnable() {
                    public void run() {
                        Random rand = new Random();
                        Ticket ticket = new Ticket();
                        int id = threadId.getAndIncrement();
                        ArrayList<Ticket> soldTicket = new ArrayList<>();
                        for (int k = 0; k < testnum; k++) {
                            int sel = rand.nextInt(inqpc);
                            if (0 <= sel && sel < retpc && soldTicket.size() > 0) { // refund ticket
                                int select = rand.nextInt(soldTicket.size());
                                if ((ticket = soldTicket.remove(select)) != null) {
                                    long s = System.currentTimeMillis();
                                    tds.refundTicket(ticket);
                                    long e = System.currentTimeMillis();
                                    refundTime[id] += e - s;
                                    refundTotal[id] += 1;
                                } else {
                                    System.out.println("ErrOfRefund");
                                }
                            } else if (routenum <= sel && sel < buypc) { // buy ticket
                                String passenger = passengerName();
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure + rand.nextInt(stationnum - departure) + 1;
                                long s = System.currentTimeMillis();
                                //只统计方法调用次数和时间，是否成功均可
                                ticket = tds.buyTicket(passenger, route, departure, arrival);
                                long e = System.currentTimeMillis();
                                buyTicketTime[id] += e - s;
                                buyTotal[id] += 1;
                                if (ticket != null) {
                                    soldTicket.add(ticket);
                                }
                            } else if (buypc <= sel && sel < inqpc) { // inquiry ticket
                                int route = rand.nextInt(routenum) + 1;
                                int departure = rand.nextInt(stationnum - 1) + 1;
                                int arrival = departure + rand.nextInt(stationnum - departure) + 1;
                                long s = System.currentTimeMillis();
                                tds.inquiry(route, departure, arrival);
                                long e = System.currentTimeMillis();
                                inquiryTime[id] += e - s;
                                inquiryTotal[id] += 1;
                            }
                        }
                    }
                });
            }
            long start = System.currentTimeMillis();
            for (int j = 0; j < threadNums[i]; ++j){
                threads[j].start();
            }

            for (int j = 0; j < threadNums[i]; j++) {
                threads[j].join();
            }

            long end = System.currentTimeMillis();
            long buyTotalTime = Sum(buyTicketTime, threadNums[i]);
            long refundTotalTime = Sum(refundTime, threadNums[i]);
            long inquiryTotalTime = Sum(inquiryTime, threadNums[i]);

            long bTotal = Sum(buyTotal, threadNums[i]);
            long rTotal = Sum(refundTotal, threadNums[i]);
            long iTotal = Sum(inquiryTotal, threadNums[i]);

            double buyAvgTime = (double) (buyTotalTime) / bTotal;
            double refundAvgTime = (double) (refundTotalTime) / rTotal;
            double inquiryAvgTime = (double) (inquiryTotalTime) / iTotal;

            long time = end - start;

            long ops = (long) (threadNums[i] * testnum / (double) time);
            
            System.out.println(String.format(
                    "ThreadNum: %d BuyAvgTime(ms): %.5f RefundAvgTime(ms): %.5f InquiryAvgTime(ms): %.5f ThroughOut(op/ms): %d",
                    threadNums[i], buyAvgTime, refundAvgTime, inquiryAvgTime, ops));
            clear();
        }
    }

    private static long Sum(long[] array, int threadNums) {
        long res = 0;
        for (int i = 0; i < threadNums; ++i)
            res += array[i];
        return res;
    }

    private static void clear() {
        threadId.set(0);
        long[][] arrays = { buyTicketTime, refundTime, inquiryTime, buyTotal, refundTotal, inquiryTotal };
        for (int i = 0; i < arrays.length; ++i)
            for (int j = 0; j < arrays[i].length; ++j)
                arrays[i][j] = 0;
    }

}
