package evotingcommon;

import java.io.Serializable;
import java.util.List;

public class RequestMessage implements Serializable{
    private int type;
    private List<String> data;

    public List<String> getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void setType(int type) {
        this.type = type;
    }
}
