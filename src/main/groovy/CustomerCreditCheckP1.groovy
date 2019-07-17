class CustomerCreditCheckP1 {
  private final SessionAPI session
  private final InteractiveAPI interactive

  CustomerCreditCheckP1(SessionAPI session, InteractiveAPI interactive) {
    this.session = session
    this.interactive = interactive
  }

  void main() {
    session.parameters.put("creditLimit", interactive.display.fields.get("WRCRLM"))
  }
}
