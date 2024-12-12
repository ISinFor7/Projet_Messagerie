package cat;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ResolutionDeNom {
	public static void main(String[] args) {
		InetAddress address;
		try {
			String host="Ordenateur";
			address=InetAddress.getByName(host);
			System.out.println(host+":"+address.getHostAddress());
		}catch(UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
