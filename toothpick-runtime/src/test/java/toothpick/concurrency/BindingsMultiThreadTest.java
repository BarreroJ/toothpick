package toothpick.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import toothpick.Factory;
import toothpick.Scope;
import toothpick.ToothPick;
import toothpick.concurrency.threads.GetInstanceThread;
import toothpick.concurrency.threads.InstallBindingThread;
import toothpick.concurrency.threads.ScopeToStringThread;
import toothpick.concurrency.threads.TestableThread;
import toothpick.concurrency.utils.ClassCreator;
import toothpick.concurrency.utils.DynamicTestClassesFactoryRegistry;
import toothpick.concurrency.utils.ThreadTestUtil;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static org.junit.Assert.assertTrue;
import static toothpick.concurrency.utils.ThreadTestUtil.STANDARD_THREAD_COUNT;

public class BindingsMultiThreadTest {

  static final String ROOT_SCOPE = "ROOT_SCOPE";
  final List<Object> scopeNames = new CopyOnWriteArrayList<>();
  private static ClassCreator classCreator = new ClassCreator();

  @Before
  public void setUp() throws Exception {
    ToothPick.openScope(ROOT_SCOPE);
    scopeNames.clear();
  }

  @After
  public void tearDown() throws Exception {
    ToothPick.reset();
    FactoryRegistryLocator.setRootRegistry(null);
    MemberInjectorRegistryLocator.setRootRegistry(null);
    ThreadTestUtil.shutdown();
  }

  @Test
  public void concurrentBindingInstall_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      InstallBindingThread runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentBindingInstallAndToString_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      TestableThread runnable;
      if (random.nextInt(100) < 20) {
        runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      } else {
        runnable = new ScopeToStringThread(ROOT_SCOPE);
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    ThreadTestUtil.shutdown();
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentBindingInstallAndGetInstance_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();
    FactoryRegistryLocator.setRootRegistry(new DynamicTestClassesFactoryRegistry(true));

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      TestableThread runnable;
      if (random.nextInt(100) < 20) {
        runnable = new InstallBindingThread(classCreator, ROOT_SCOPE);
      } else {
        runnable = new GetInstanceThread(ROOT_SCOPE, classCreator.allClasses[random.nextInt(classCreator.allClasses.length)]);
      }
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    boolean timeout = ThreadTestUtil.shutdown();
    assertTrue("Executor service should not timeout.", timeout);
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }

  @Test
  public void concurrentScopedGetInstance_shouldNotCrash() throws InterruptedException {
    //GIVEN
    final int addNodeThreadCount = STANDARD_THREAD_COUNT;
    List<TestableThread> threadList = new ArrayList<>();
    Random random = new Random();
    FactoryRegistryLocator.setRootRegistry(new DynamicTestClassesFactoryRegistry(true));

    //WHEN
    for (int indexThread = 0; indexThread < addNodeThreadCount; indexThread++) {
      TestableThread runnable = new GetInstanceThread(ROOT_SCOPE, classCreator.allClasses[random.nextInt(classCreator.allClasses.length)]);
      threadList.add(runnable);
      ThreadTestUtil.submit(runnable);
    }

    //THEN
    //we simply should not have crashed when all threads are done
    boolean timeout = ThreadTestUtil.shutdown();
    assertTrue("Executor service should not timeout.", timeout);
    for (TestableThread thread : threadList) {
      assertTrue(String.format("test of thread %s failed", thread.getName()), thread.isSuccessful());
    }
  }
}
