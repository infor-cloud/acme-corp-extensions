import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class CustomerCreditCheckTest extends GroovyTestCase {
  private CustomerCreditCheckP2 customerCreditCheck
  private LoggerAPI mockedLogger
  private InteractiveAPI mockedInteractive
  private DisplayAPI mockedDisplay
  private SessionAPI mockedSession
  private MICallerAPI mockedMiCaller

  @Override
  protected void setUp() throws Exception {
    mockedLogger = mock(LoggerAPI)
    mockedInteractive = mock(InteractiveAPI)
    mockedDisplay = mock(DisplayAPI)
    when(mockedInteractive.display).thenReturn(mockedDisplay)
    mockedSession = mock(SessionAPI)
    mockedMiCaller = mock(MICallerAPI)
    customerCreditCheck = new CustomerCreditCheckP2(mockedLogger, mockedSession, mockedInteractive, mockedMiCaller)
  }

  @Test
  void test_customerHasStoppedOrders_api_call() {
    // Verify that search head API is called once
    customerCreditCheck.customerHasStoppedOrders("CUST-01")
    verify(mockedMiCaller, times(1)).call(eq("OIS100MI"), eq("SearchHead"), any(), any())
  }

  @Test
  void test_checkStoppedOrders() {
    // Test non existing previous value
    boolean checkStoppedOrders_noPrev = customerCreditCheck.checkStoppedOrders(null, "0")
    assertFalse(checkStoppedOrders_noPrev) // Should not continue since no previous value was found
    boolean checkStoppedOrders_prevMore = customerCreditCheck.checkStoppedOrders("500", "100")
    assertFalse(checkStoppedOrders_prevMore) // Decrease in credit limit should be ok i.e. no need to continue
    boolean checkStoppedOrders_prevEqual = customerCreditCheck.checkStoppedOrders("500", "500")
    assertFalse(checkStoppedOrders_prevEqual) // Equal credit limit should be ok i.e. no need to continue
    boolean checkStoppedOrders_prevLess = customerCreditCheck.checkStoppedOrders("100", "500")
    assertTrue(checkStoppedOrders_prevLess) // Increase in credit limit means we _should_ check!
  }

  @Test
  void test_customerHasStoppedOrders() {
    // Verify that search head API is called once
    customerCreditCheck.customerHasStoppedOrders("CUST-01")
    verify(mockedMiCaller, times(1)).call(eq("OIS100MI"), eq("SearchHead"), any(), any())
    // Simulate return of a MI record, should make extension think that there's at least one record
    when(mockedMiCaller.call(eq("OIS100MI"), eq("SearchHead"), any(), any())).thenAnswer(new Answer<Object>() {
      @Override
      Object answer(InvocationOnMock invocation) throws Throwable {
        Closure<?> closure = invocation.getArgument(3, Closure.class)
        closure.call(new HashMap())
        return null
      }
    })
    // Should indicate that customer has stopped Orders
    boolean customerHasStoppedOrders = customerCreditCheck.customerHasStoppedOrders("CUST-01")
    assertTrue(customerHasStoppedOrders)
  }

  @Test
  void test_main() {
    // Test whole extension, simulate entry
    Map<String, Object> parameters = new HashMap<>()
    parameters.put("creditLimit", 1000)
    when(mockedSession.parameters).thenReturn(parameters)
    Map<String, Object> displayFields = new HashMap<>()
    displayFields.put("WWCUNO", "CUST-01")
    displayFields.put("WRCRLM", "5000")
    when(mockedDisplay.fields).thenReturn(displayFields)
    when(mockedMiCaller.call(eq("OIS100MI"), eq("SearchHead"), any(), any())).thenAnswer(new Answer<Object>() {
      @Override
      Object answer(InvocationOnMock invocation) throws Throwable {
        Closure<?> closure = invocation.getArgument(3, Closure.class)
        closure.call(new HashMap())
        return null
      }
    })
    customerCreditCheck.main()
    verify(mockedInteractive, times(1)).showCustomError(eq("WRCRLM"), eq("Credit limit reached!"))
  }
}
