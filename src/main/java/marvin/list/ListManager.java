package marvin.list;

public interface ListManager  {
    /**
     * If the list is unseen, adds a record to the database and returns true;
     * If the list has been seen, and
     * - if the list was last downloaded more than 24 hours ago, updates the database record and returns true
     * - if the list was downloaded less than 24 hours ago, returns false
     * @param key
     * @return true if the list should be refreshed, false otherwise
     */
    boolean add(String key);
}
