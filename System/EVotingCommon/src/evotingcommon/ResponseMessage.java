/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evotingcommon;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Maciek
 */
public class ResponseMessage implements Serializable{
    int status;
    List<String> data;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
