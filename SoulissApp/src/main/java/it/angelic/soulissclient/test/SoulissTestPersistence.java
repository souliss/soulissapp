package it.angelic.soulissclient.test;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import java.util.List;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.db.SoulissDB;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.SoulissTypicalFactory;
import it.angelic.soulissclient.model.typicals.SoulissTypical11DigitalOutput;
import it.angelic.soulissclient.model.typicals.SoulissTypical51AnalogueSensor;

/**
 * Created by shine@angelic.it on 02/09/2015.
 */
public class SoulissTestPersistence extends AndroidTestCase {
    private static final short fakeNodeId = 1;
    private static final short fakeSlotId = 1;
    private SoulissDBHelper db;
    private SoulissPreferenceHelper opzioni;

    protected void addFakeNode() {
        SoulissNode testNode = new SoulissNode(fakeNodeId);
        // Here i have my new database wich is not connected to the standard database of the App
        db.createOrUpdateNode(testNode);
        assertEquals(1, db.countNodes());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    protected void addFakeLight() {
        SoulissTypical11DigitalOutput testTypical = new SoulissTypical11DigitalOutput(opzioni);
        testTypical.getTypicalDTO().setTypical(Constants.Typicals.Souliss_T11);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot(fakeSlotId);
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);

        assertEquals(1, testTypical.getTypicalDTO().persist());
        // Here i have my new database wich is not connected to the standard database of the App
    }

    protected void addFakeSensor() {
        SoulissTypical51AnalogueSensor testTypical = new SoulissTypical51AnalogueSensor(opzioni);
        testTypical.getTypicalDTO().setTypical(Constants.Typicals.Souliss_T51);
        testTypical.getTypicalDTO().setNodeId(fakeNodeId);
        testTypical.getTypicalDTO().setSlot((short) (fakeSlotId + 1));
        SoulissNode father = db.getSoulissNode(fakeNodeId);
        testTypical.setParentNode(father);
        testTypical.getTypicalDTO().persist();
        //assertEquals(1, );
        // Here i have my new database wich is not connected to the standard database of the App
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new SoulissDBHelper(context);
        opzioni = new SoulissPreferenceHelper(context);
        SoulissDBHelper.open();

        addFakeNode();
        addFakeLight();
        addFakeSensor();
    }

    @Override
    public void tearDown() throws Exception {
        getContext().deleteDatabase(SoulissDB.DATABASE_NAME);

        Log.i(Constants.TAG, "tearDown test DB");
        db.close();
        super.tearDown();

    }

    public void testGetAllNodes() {
        List<SoulissNode> testList = db.getAllNodes();
        assertEquals(testList.size(), db.countNodes());
    }

    public void testLogging() {
        SoulissTypical11DigitalOutput testTypical = (SoulissTypical11DigitalOutput) db.getTypical(fakeNodeId, fakeSlotId);

        testTypical.getTypicalDTO().setOutput(Constants.Typicals.Souliss_T1n_OnCoil);

        testTypical.getTypicalDTO().refresh(testTypical);

        assertTrue(db.getTypicalLogs(testTypical).size() > 0);

    }

    public void testGetSize() {
        assertTrue(db.getSize() > 0);
    }

    public void testGetNodeTypicals() {

        SoulissNode father = db.getSoulissNode(fakeNodeId);
        List<SoulissTypical> testTypical = db.getNodeTypicals(father);

        assertEquals(2, db.countTypicals());//siam sicuri che solo lui
        SoulissTypical51AnalogueSensor copy = (SoulissTypical51AnalogueSensor) SoulissTypicalFactory.getTypical(Constants.Typicals.Souliss_T51, father, testTypical.get(1).getTypicalDTO(), new SoulissPreferenceHelper(getContext()));

        assertEquals(copy.getTypicalDTO(), testTypical.get(1).getTypicalDTO());
    }

    public void testGetTypical() {
        SoulissTypical51AnalogueSensor testTypical = (SoulissTypical51AnalogueSensor) db.getTypical(fakeNodeId, (short) (fakeSlotId + 1));

        SoulissNode father = db.getSoulissNode(fakeNodeId);
        SoulissTypical51AnalogueSensor copy = (SoulissTypical51AnalogueSensor) SoulissTypicalFactory.getTypical(Constants.Typicals.Souliss_T51, father, testTypical.getTypicalDTO(), new SoulissPreferenceHelper(getContext()));

        assertEquals(true, testTypical.isSensor());
        assertEquals(copy.getTypicalDTO(), testTypical.getTypicalDTO());

    }
}
