package d.d.meshenger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


public class Contact implements Serializable {
    enum State { ONLINE, OFFLINE, PENDING };

    private String name;
    private boolean blocked;
    private List<String> addresses;

    // contact state
    private State state = State.PENDING;

    // last working address (use this address next connection and for unknown contact initialization)
    private InetSocketAddress last_working_address = null;

    public Contact(String name, byte[] pubkey, List<String> addresses) {
        this.name = name;
        this.blocked = false;
        this.addresses = addresses;
    }

    private Contact() {
        this.name = "";
        this.blocked = false;
        this.addresses = new ArrayList<>();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<String> getAddresses() {
        return this.addresses;
    }

    public void addAddress(String address) {
        if (address.isEmpty()) {
            return;
        }

        for (String addr : this.addresses) {
            if (addr.equalsIgnoreCase(address)) {
                return;
            }
        }
        this.addresses.add(address);
    }

    public List<InetSocketAddress> getAllSocketAddresses() {
        List<InetSocketAddress> addrs = new ArrayList<>();
        for (String address : this.addresses) {
            try {
                if (Utils.isMAC(address)) {
                    addrs.addAll(Utils.getAddressPermutations(address, MainService.serverPort));
                } else {
                    // also resolves domains
                    addrs.add(Utils.parseInetSocketAddress(address, MainService.serverPort));
                }
            } catch (Exception e) {
                log("invalid address: " + address);
                e.printStackTrace();
            }
        }

        return addrs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    private static Socket establishConnection(InetSocketAddress address, int timeout) {
        Socket socket = new Socket();
        try {
            // timeout to establish connection
            socket.connect(address, timeout);
            return socket;
        } catch (SocketTimeoutException e) {
            // ignore
        } catch (ConnectException e) {
            // device is online, but does not listen on the given port
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                // ignore
            }
        }

        return null;
    }

    /*
    * Create a connection to the contact.
    * Try/Remember the last successful address.
    */
    public Socket createSocket() {
        Socket socket = null;
        int connectionTimeout = 500;

        // try last successful address first
        if (this.last_working_address != null) {
            log("try latest address: " + this.last_working_address);
            socket = this.establishConnection(this.last_working_address, connectionTimeout);
            if (socket != null) {
                return socket;
            }
        }

        for (InetSocketAddress address : this.getAllSocketAddresses()) {
            log("try address: '" + address.getHostName() + "', port: " + address.getPort());
            socket = this.establishConnection(address, connectionTimeout);
            if (socket != null) {
                return socket;
            }
        }

        return null;
    }

    // set good address to try first next time
    public void setLastWorkingAddress(InetSocketAddress address) {
        log("setLatestWorkingAddress: " + address);
        this.last_working_address = address;
    }

    public InetSocketAddress getLastWorkingAddress() {
        return this.last_working_address;
    }

    public static JSONObject exportJSON(Contact contact, boolean all) throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();

        object.put("name", contact.name);

        for (String address : contact.getAddresses()) {
            array.put(address);
        }
        object.put("addresses", array);

        if (all) {
            object.put("blocked", contact.blocked);
        }

        return object;
    }

    public static Contact importJSON(JSONObject object, boolean all) throws JSONException {
        Contact contact = new Contact();

        contact.name = object.getString("name");

        if (!Utils.isValidName(contact.name)) {
            throw new JSONException("Invalid Name.");
        }

        JSONArray array = object.getJSONArray("addresses");
        for (int i = 0; i < array.length(); i += 1) {
            contact.addAddress(array.getString(i).toUpperCase().trim());
        }

        if (all) {
            contact.blocked = object.getBoolean("blocked");
        }

        return contact;
    }

    private void log(String s) {
        Log.d(this, s);
    }
}
