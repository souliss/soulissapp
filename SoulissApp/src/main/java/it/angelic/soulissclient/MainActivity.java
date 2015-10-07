package it.angelic.soulissclient;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.Collections;
import java.util.List;

import it.angelic.soulissclient.adapters.StaggeredAdapter;
import it.angelic.soulissclient.db.SoulissDBLauncherHelper;
import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.LauncherElement;

/**
 * This will not work so great since the heights of the imageViews
 * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get
 * the (intrinsic) image width and height and set the views height manually. I will
 * look into a fix once I find extra time.
 *
 * @author Maurycy Wojtowicz
 */
public class MainActivity extends AbstractStatusedFragmentActivity {

    private RecyclerView mRecyclerView;
    private SoulissPreferenceHelper opzioni;
    /**
     * Images are taken by Romain Guy ! He's a great photographer as well as a
     * great programmer. http://www.flickr.com/photos/romainguy
     */

    /**
     * This will not work so great since the heights of the imageViews
     * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get
     * the (intrinsic) image width and height and set the views height manually. I will
     * look into a fix once I find extra time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissApp.getOpzioni();
        // Remove title bar
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (opzioni.isLightThemeSelected())
            setTheme(R.style.LightThemeSelector);
        else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_launcher2);
        // final Button buttAddProgram = (Button)
        // findViewById(R.id.buttonAddScene);
        // tt = (TextView) findViewById(R.id.TextViewScenes);
        /*
         * if ("def".compareToIgnoreCase(opzioni.getPrefFont()) != 0) { Typeface
		 * font = Typeface.createFromAsset(getAssets(), opzioni.getPrefFont());
		 * tt.setTypeface(font, Typeface.NORMAL); }
		 */

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewLauncherItems);


        StaggeredGridLayoutManager gm = new StaggeredGridLayoutManager(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(gm);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//FIXME

        SoulissDBLauncherHelper dbLauncher = new SoulissDBLauncherHelper(this);
        List vette = dbLauncher.getLauncherItems(this);
        LauncherElement[] array = (LauncherElement[]) vette.toArray(new LauncherElement[vette.size()]);

        final StaggeredAdapter adapter = new StaggeredAdapter(array);

        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // DRAWER
        super.initDrawer(this, DrawerMenuHelper.TAGS);

        // Extend the Callback class
        ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
                //  return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                //        ItemTouchHelper.DOWN | ItemTouchHelper.UP |);
            }

            //and in your imlpementaion of
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them

                Collections.swap(adapter.getList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                // and notify the adapter that its dataset has changed
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                //SoulissTag todoItem = adapter.getItem(viewHolder.getAdapterPosition());
                //forse non serve

                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                //clearView(mRecyclerView, viewHolder);
            }

        };
// Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(mRecyclerView);

    }

}