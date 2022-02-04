import java.io.IOException;

import org.brunocvcunha.opennode.api.OpenNodeService;
import org.brunocvcunha.opennode.api.OpenNodeServiceFactory;
import org.brunocvcunha.opennode.api.model.OpenNodeCharge;
import org.brunocvcunha.opennode.api.model.OpenNodeCreateCharge;

public class Main {

	public static void main(String[] args) {
		OpenNodeService service = OpenNodeServiceFactory.buildClient("e6c98559-e1a8-473a-abcf-0dfb73211df1");

		OpenNodeCreateCharge createCharge = OpenNodeCreateCharge.builder().orderId("ABC-123")
				.description("OpenNode Java Client").amount(10).build();

		try {
			OpenNodeCharge createdCharge = service.createCharge(createCharge).execute().body().getData();
			System.out.println("Created charge with ID: " + createdCharge.getId());
			System.out.println("Invoice: " + createdCharge.getLightningInvoice().getPayreq());

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
