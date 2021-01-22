package d.d.meshenger;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/*
 * Item for address management (AddressActivity)
 */
public class AddressEntry implements Comparable<AddressEntry> {
    InetAddress address;
    String device;
    boolean multicast;

    AddressEntry(InetAddress address, String device, boolean multicast) {
        this.address = address;
        this.device = device;
        this.multicast = multicast;
    }

    @Override
    public int compareTo(AddressEntry e) {
        return Arrays.equals(this.address.getAddress(),e.address.getAddress())? 0:-1;
    }

    @Override
    public String toString() {
        return this.address.getHostAddress();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AddressEntry) {
            return Arrays.equals(this.address.getAddress(),((AddressEntry) other).address.getAddress());
        } else {
            return super.equals(other);
        }
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }

    public static AddressEntry findAddressEntry(List<AddressEntry> list, String address) {
      for (int i = 0; i < list.size(); i += 1) {
        if (list.get(i).address.equals(address)) {
          return list.get(i); 
        }
      }
      return null;
    }

    public static int listIndexOf(List<AddressEntry> list, AddressEntry entry) {
        Iterator<AddressEntry> iter = list.listIterator();
        int i = 0;

        while(iter.hasNext()) {
            if (iter.next().address.equals(entry.address)) {
                return i;
            }
            i += 1;
        }
        return -1;
    }
}
