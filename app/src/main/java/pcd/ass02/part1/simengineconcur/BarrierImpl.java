package pcd.ass02.part1.simengineconcur;

import pcd.ass02.part1.simengineseq.AbstractSimulation;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierImpl implements Barrier {

  private final int nThreads;
  private ReentrantLock mutex;
  private Condition cond;
  private int nWait = 0;
  private int nPassed = 0;


  public BarrierImpl(int nThreads) {
    this.nThreads = nThreads;
    this.mutex = new ReentrantLock();
    this.cond = mutex.newCondition();
  }

  /**
   * Wait until all threads are ready for the next phase
   * @param sim
   */
  @Override
  public void waitBefore(AbstractSimulation sim) {
    try {
      mutex.lock();
      nWait++;
      while(sim.isStopped()) {
        // Wait
      }
//      System.out.println(nWait);
      if (nWait < nThreads) {
        do {
          cond.await();
        } while (nPassed == 0);
      } else {
        nWait = 0; // Reset of the barrier.
        cond.signalAll();
      }
      nPassed = (nPassed + 1) % nThreads;

    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      mutex.unlock();
    }
  }

  public void signalAll() {
    cond.signalAll();
  }
}
