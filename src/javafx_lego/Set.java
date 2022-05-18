package javafx_lego;

import javafx.beans.property.SimpleStringProperty;

public class Set {
private  SimpleStringProperty set_num;
private  SimpleStringProperty quantity;

Set(String snum, String q) {
	this.set_num = new SimpleStringProperty(snum);
	this.quantity = new SimpleStringProperty(q);

}
public String getSet_num(){
	return set_num.get();
}

public void setSet_num(String snum) {
	set_num.set(snum);
}

public String getQuantity(){
	return quantity.get();
}

public void setQuantity(String q) {
	quantity.set(q);
}
}
