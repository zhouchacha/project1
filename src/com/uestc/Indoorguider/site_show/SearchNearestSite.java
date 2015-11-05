package com.uestc.Indoorguider.site_show;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.uestc.Indoorguider.IndoorGuiderApplication;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeyMissingException;
import edu.wlu.cs.levy.CG.KeySizeException;
public class SearchNearestSite {

	IndoorGuiderApplication mApplication;
	private final int sitesNum = 6;
	private final float nearestDistance = 600;  //间距
	double[][] sites;
	KDTree<Integer> kd;
	//使用单例模式
	private static final SearchNearestSite serchNearest = new SearchNearestSite();
	public static SearchNearestSite getInstance(){		
		return serchNearest;
	}

	public String findClickedNearestSite(SiteInfo goal) 
			throws KeySizeException, KeyDuplicateException, KeyMissingException {
		
		String result = null;
		double goalX = goal.getX();
		double goalY = goal.getY();
		Log.v("kd", "in find");
		
		initKDtree();
		List<Integer> tmpData = new ArrayList<Integer>();
		//Log.v("kd", ""+kd.toString());
		Log.v("kd", "test");
		for (int i = 0; i < sitesNum; ++i) {
			
			Log.v("kd", "in for");
			int m = kd.nearest(new double[] {goalX, goalY});
			tmpData.add(m);
			int left = mApplication.sitesApplication.get(m).getLeft();
			int right = mApplication.sitesApplication.get(m).getRight();
			int top = mApplication.sitesApplication.get(m).getTop();
			int buttom = mApplication.sitesApplication.get(m).getButtom();
			
			Log.v("kd", "goal x: " + goalX);
			Log.v("kd", "goal y: " + goalY);
			Log.v("kd", "left: " + left);
			Log.v("kd", "right: " + right);
			Log.v("kd", "top: " + top);
			Log.v("kd", "buttom: " + buttom);
			Log.v("kd", "name: " + mApplication.sitesApplication.get(m).getSiteName());
			kd.delete(sites[m]);
			if (goalX > left && goalX < right && goalY > top && goalY < buttom) {
				result = mApplication.sitesApplication.get(m).getSiteName();
				break;
			}
		}

		for (int i = 0; i < tmpData.size(); ++i) {			
			int num = tmpData.get(i);
			kd.insert(sites[num], num);
		}
		tmpData = null;
		Log.v("kd", "result: " + result);
		return mApplication.sitesNameEnAndChinese.get(result);
	}
	
	public void initKDtree() throws KeySizeException, KeyDuplicateException{
		kd = new KDTree<Integer>(2);
		int len = mApplication.sitesApplication.size();
		sites = new double[len][2];
		for (int i = 0; i < len; ++i) {
			sites[i][0] = mApplication.sitesApplication.get(i).getX();
			sites[i][1] = mApplication.sitesApplication.get(i).getY();
			kd.insert(sites[i], i);
		}
	}
	
}
