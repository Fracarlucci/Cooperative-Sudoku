package pcd.ass03.part1.simengineconcur;

import pcd.ass03.part1.simengineseq.AbstractSimulation;

/**
 * Interface for a barrier to synchronize threads
 *
 */
public interface Barrier {
  
  public void waitBefore(AbstractSimulation isStopped);

  void signalAll();
}
