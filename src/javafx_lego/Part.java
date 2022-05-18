package javafx_lego;

import javafx.beans.property.SimpleStringProperty;

public class Part {
private  SimpleStringProperty part_num;
private  SimpleStringProperty color_id;
private  SimpleStringProperty quantity;

Part(String pnum, String cid, String q) {
	this.part_num = new SimpleStringProperty(pnum);
	this.color_id = new SimpleStringProperty(cid);
	this.quantity = new SimpleStringProperty(q);

}
public String getPart_num(){
	return part_num.get();
}

public void setPart_num(String pnum) {
	part_num.set(pnum);
}

public String getColor_id(){
	return color_id.get();
}

public void setColor_id(String cid) {
	color_id.set(cid);
}

public String getQuantity(){
	return quantity.get();
}

public void setQuantity(String q) {
	quantity.set(q);
}
}
