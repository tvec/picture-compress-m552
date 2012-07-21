package tony.vec;

import android.graphics.Bitmap;
/**
 * 
 * @author tony
 *
 */
public class PictureResults {

	String filePath;
	int height, width;
	
	Bitmap color,results;

	public PictureResults(String fp) {
		this.filePath = fp;
		
	}
	public void setBitmap(Bitmap bm){
		color= bm;
	}
	public Bitmap getBitmap(){
		return color;
	}
	public void  setHeight(int h){
		height = h;
	}

	public int getHeight(){
		return height;
	}
	public void  setWidth(int w){
		width = w;
	}
	public int getWidth(){
		return width;
	}
	
}
