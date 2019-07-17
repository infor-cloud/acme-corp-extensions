class CustomerCreditCheckP2 {
  private final LoggerAPI logger
  private final SessionAPI session
  private final InteractiveAPI interactive
  private final MICallerAPI miCaller

  CustomerCreditCheckP2(LoggerAPI logger, SessionAPI session, InteractiveAPI interactive, MICallerAPI miCaller) {
    this.logger = logger
    this.session = session
    this.interactive = interactive
    this.miCaller = miCaller
  }

  void main() {
    String creditLimit = session.parameters.get("creditLimit")
    if (creditLimit == null) {
      logger.info("No previous credit limit info was found")
    }
    if (!checkStoppedOrders(creditLimit, interactive.display.fields.get("WRCRLM").toString())) {
      return
    }
    if (customerHasStoppedOrders(interactive.display.fields.WWCUNO.toString().trim())) {
      interactive.showCustomError("WRCRLM", "Credit limit reached!")
    }
  }

  boolean checkStoppedOrders(String prev, String curr) {
    if (prev == null || curr == null) {
      return false
    }
    double previousCreditLimit = Double.parseDouble(prev.trim().replace(",", "."))
    double currentCreditLimit = Double.parseDouble(curr.trim().replace(",", "."))
    return previousCreditLimit < currentCreditLimit
  }

  boolean customerHasStoppedOrders(String customer) {
    def parameters = ["SQRY": "OBLC:8 AND CUNO:" + customer]
    int stoppedOrders = 0
    Closure<?> handler = {
      Map<String, String> response -> stoppedOrders++
    }
    miCaller.call("OIS100MI", "SearchHead", parameters, handler)
    return stoppedOrders != 0
  }
}
