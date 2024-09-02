package pcd.ass02.part1.simengineconcur;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * readers/writers Monitor (Threads/GUI)
 *
*/
public class GUIMonitor {
    private int nReaders;
    private int nWriters;
    private final Condition okToRead;
    private final Condition okTowrite;
    private ReentrantLock mutex;

    public GUIMonitor(){
        this.nReaders = 0;
        this.nWriters = 0;
        this.mutex = new ReentrantLock();
        this.okToRead = mutex.newCondition();
        this.okTowrite = mutex.newCondition();
    }

    public void requestRead(){
        mutex.lock();
        try {
            while(this.nWriters > 0){
                okToRead.await();
            }
            this.nReaders++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public void releaseRead(){
        mutex.lock();
        try {
            this.nReaders--;
            if (this.nReaders == 0) {
                this.okTowrite.signalAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public void requestWrite(){
        try {
            mutex.lock();
            while(this.nReaders > 0 || this.nWriters > 0){
                okTowrite.await();
            }
            this.nWriters++;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public void releaseWrite(){
        try {
            mutex.lock();
            this.nWriters--;
            this.okTowrite.signal();
            this.okToRead.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }
}
