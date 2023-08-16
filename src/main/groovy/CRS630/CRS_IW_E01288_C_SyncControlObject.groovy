/**
 * README
 * This extension is used by CRS630/PCUPD/POST.
 *
 * Name: CRS_IW_E01288_C_SyncControlObject
 * Description: This extension transfers the information from CRS630 to the CRS335
 * Date	      Changed By			      Description
 * 20211206   Joana Marie Sabino    EOS-1288  – Collaboration / Context Accounting Identity (CRS630) and Control Object (CRS335)
 * 20220209   Joana Marie Sabino    Added length checking for retrieved AITM
 */
public class CRS_IW_E01288_C_SyncControlObject extends ExtendM3Trigger {
  private final ProgramAPI program;
  private final LoggerAPI logger;
  private final InteractiveAPI interactive;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;

  // Global var
  int XXCONO = 0;
  String XXAITM;

  public CRS_IW_E01288_C_SyncControlObject(ProgramAPI program, LoggerAPI logger, InteractiveAPI interactive, DatabaseAPI database, MICallerAPI miCaller) {
    this.program = program;
    this.logger = logger;
    this.interactive = interactive;
    this.database = database;
    this.miCaller = miCaller;
  }

  public void main() {
    XXCONO = program.LDAZD.CONO as int;
    DBContainer FCHACC = database.getContainer("FCHACC");
    if (FCHACC.getInt("EAAITP") != 4) {
      return;
    }
    Map<String, String> record = getControlObject();

    if (record.equals([:])) {
      addControlObject();
    } else {
      if (FCHACC.getString("EATX40") != record["TX40"].trim() || FCHACC.getString("EATX15") != record["TX15"].trim()) {
        updateControlObject();
      }
    }
  }

  /**
   * Retrieve Control Object
   */
  Map<String, String> getControlObject() {
    DBContainer FCHACC = database.getContainer("FCHACC");
    Map<String, String> record = [:];
    def callback = {
      Map <String, String> result ->
        if (result.error == null) {
          record = result;
        }
    }

    XXAITM = FCHACC.getString("EAAITM").replaceAll("\\s","");
    if(XXAITM.length() > 8){
      XXAITM = XXAITM.substring(0,8);
    }

    def params = [ "ACRF": XXAITM ];
    miCaller.call("CRS335MI", "GetCtrlObj", params, callback);
    return record;
  }

  /**
   * Add Control Object
   */
  void addControlObject() {
    def callback = {
        // no error handling
      Map <String, String> result ->
        if (result.error != null) {
          logger.debug("EOS-1288 Error ${result}");
        }
    }

    DBContainer FCHACC = database.getContainer("FCHACC");
    XXAITM = FCHACC.getString("EAAITM").replaceAll("\\s","");
    if(XXAITM.length() > 8){
      XXAITM = XXAITM.substring(0,8);
    }

    def params = [ "ACRF": XXAITM,
                   "TX40": FCHACC.getString("EATX40"),
                   "TX15": FCHACC.getString("EATX15")
    ];

    miCaller.call("CRS335MI", "AddCtrlObj", params, callback);
  }

  /**
   * Update Control Object
   */
  void updateControlObject() {
    def callback = {
        // no error handling
      Map <String, String> result ->
        if (result.error != null) {
          logger.debug("EOS-1288 Error ${result}");
        }
    }

    DBContainer FCHACC = database.getContainer("FCHACC");
    XXAITM = FCHACC.getString("EAAITM").replaceAll("\\s","");
    if(XXAITM.length() > 8){
      XXAITM = XXAITM.substring(0,8);
    }
    def params = [ "ACRF": XXAITM,
                   "TX40": FCHACC.getString("EATX40"),
                   "TX15": FCHACC.getString("EATX15")
    ];

    miCaller.call("CRS335MI", "UpdCtrlObj", params, callback);
  }
}
