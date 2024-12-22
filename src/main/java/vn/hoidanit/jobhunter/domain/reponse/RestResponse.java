package vn.hoidanit.jobhunter.domain.reponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {
    private int statusCode;
    private String error;

    // message có thể là String hay Arraylist
    private Object message;
    private T data;
    
}
