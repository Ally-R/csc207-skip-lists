import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;
import java.lang.Math;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {
  // +------+--------------------------------------------------------
  // | Main |
  // +------+

  /**
   * Tests to find Big O for SkipLists
   */
  public static void main(String[] args) throws Exception {
    PrintWriter pen = new PrintWriter(System.out, true);
    int trials = 6;
    int avSetCounts[] = new int[trials];
    int avGetCounts[] = new int[trials];
    int avRemCounts[] = new int[trials];
    for (int lst = 1; lst <= trials; lst++) {
      int setCounts[] = new int[4];
      int getCounts[] = new int[4];
      int remCounts[] = new int[4];
      double lstlen = 500 * Math.pow(2, lst - 1); // List size
      for (int test = 1; test <= 4; test++) {
        SkipList<Integer, String> sklst = new SkipList<Integer, String>((i, j) -> i - j);

        // Run the operations of interest on the list
        for (int i = 0; i < lstlen; i++) {
          sklst.set(i, SkipListTests.value(i));
          sklst.get(i);
        } // for
        for (int i = 0; i < lstlen; i++) {
          sklst.remove(i);
        } // for

        // Log the raw data
        setCounts[test - 1] = sklst.setCount;
        getCounts[test - 1] = sklst.getCount;
        remCounts[test - 1] = sklst.remCount;

        // Print the raw data
        /*
         * pen.println("List length " + (int) lstlen);
         * pen.println(" Set = " + sklst.setCount);
         * pen.println(" Get = " + sklst.getCount);
         * pen.println(" Remove = " + sklst.remCount);
         * pen.println();
         */

      } // for 4 test cycles

      // Calculate the averages
      avSetCounts[lst - 1] = ((setCounts[0] + setCounts[1] + setCounts[2] + setCounts[3]) / 4);
      avGetCounts[lst - 1] = ((getCounts[0] + getCounts[1] + getCounts[2] + getCounts[3]) / 4);
      avRemCounts[lst - 1] = ((remCounts[0] + remCounts[1] + remCounts[2] + remCounts[3]) / 4);

      // Print header
      pen.println("-----------------------");
      pen.println("List length " + (int) lstlen);
      pen.println("-----------------------");
      
      // Print the averages
      pen.println("Averages:");
      pen.println(" * Set = " + avSetCounts[lst - 1]);
      pen.println(" * Get = " + avGetCounts[lst - 1]);
      pen.println(" * Remove = " + avRemCounts[lst - 1]);
      pen.println();

      // Print the differences between list size and average
      pen.println("List Size / Average:");
      pen.println(" * Set = " + (lstlen / avSetCounts[lst - 1]));
      pen.println(" * Get = " + (lstlen / avGetCounts[lst - 1]));
      pen.println(" * Remove = " + (lstlen / avRemCounts[lst - 1]));
      pen.println();
    } // for trails number of lists
  } // main

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The initial height of the skip list.
   */
  static final int INITIAL_HEIGHT = 16;

  // +---------------+-----------------------------------------------
  // | Static Fields |
  // +---------------+

  static Random rand = new Random();

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Pointers to all the front elements.
   */
  ArrayList<SLNode<K, V>> front;

  /**
   * The comparator used to determine the ordering in the list.
   */
  Comparator<K> comparator;

  /**
   * The number of values in the list.
   */
  int size;

  /**
   * The current height of the skiplist.
   */
  int height;

  /**
   * The probability used to determine the height of nodes.
   */
  double prob = 0.5;

  /**
   * The counter of core operations in set method: 
   *  - switch a level
   *  - move across the list on the current level.
   */
  int setCount = 0;

  /**
   * The counter of core operations in get method:
   *   - switch a level
   *   - move across the list on the current level.
   */
  int getCount = 0;

  /**
   * The counter of core operations in remove method:
   *   - switch a level
   *   - move across the list on the current level.
   */
  int remCount = 0;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new skip list that orders values using the specified comparator.
   */
  public SkipList(Comparator<K> comparator) {
    this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
    for (int i = 0; i < INITIAL_HEIGHT; i++) {
      front.add(null);
    } // for
    this.comparator = comparator;
    this.size = 0;
    this.height = INITIAL_HEIGHT;
  } // SkipList(Comparator<K>)

  /**
   * Create a new skip list that orders values using a not-very-clever default comparator.
   */
  public SkipList() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SkipList()


  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  /**
   * Set the value associated with key.
   * 
   * @return the previous value associated with key (or null, if there's no such value)
   * 
   * @throws NullPointerException if the key is null.
   */
  @Override
  public V set(K key, V value) {
    if (key == null) {
      throw new NullPointerException();
    } // if key null

    // array of prev pointers
    ArrayList<SLNode<K, V>> prev = new ArrayList<SLNode<K, V>>(this.height);
    for (int i = 0; i < this.height; i++) {
      prev.add(null);
    } // for (initialize prev array)

    // Search SkipList for key
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current == null && lvl > 0) {
        current = this.front.get(lvl - 1);
        setCount++;
      } // if
      else if (current != null) {
        if (current == this.front.get(lvl) && this.comparator.compare(current.key, key) == 0) {
          V temp = current.value;
          current.value = value;
          return temp;
        } else if (current == this.front.get(lvl)
            && this.comparator.compare(current.key, key) > 0) {
          if (lvl > 0) {
            current = this.front.get(lvl - 1);
            setCount++;
          } // if not already at bottom level, move down a level
        } // else if
        else {
          setCount++;
          while (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
            setCount++;
            current = current.next.get(lvl);
            setCount++;
          } // while current < key at level lvl
          prev.set(lvl, current); // add prev pointer to prev array
          setCount++;
          if (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
            setCount++;
            setCount++;           
            current = current.next.get(lvl);
            setCount++;
            V temp = current.value;
            current.value = value;
            return temp;
          } // if found key, update value
        } // else
      } // if current not null
    } // for each level

    // Set new node if applicable
    SLNode<K, V> setNode = new SLNode<K, V>(key, value, this.randomHeight());
    for (int lvl = this.height; lvl <= setNode.next.size(); lvl++) {
      this.front.add(setNode);
      setNode.next.set(lvl - 1, null);
    } // if node largest yet
    if (this.height < setNode.next.size()) {
      this.height = setNode.next.size();
    } // if (update height)
    for (int lvl = 0; lvl < setNode.next.size() && lvl < prev.size(); lvl++) {
      setCount++;
      if (prev.get(lvl) == null) {
        setNode.next.set(lvl, front.get(lvl));
        front.set(lvl, setNode);
      } else {
        setNode.next.set(lvl, prev.get(lvl).next.get(lvl));
        prev.get(lvl).next.set(lvl, setNode);
      } // else
    } // for (initialize new node)
    this.size++;
    return null;
  } // set(K,V)

  /**
   * Get the value associated with key.
   * 
   * @throws IndexOutOfBoundsException if the key is not in the map.
   * @throws NullPointerException if the key is null.
   */
  @Override
  public V get(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    // Search SkipList for key
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current == null && lvl > 0) {
        current = this.front.get(lvl - 1);
        getCount++;
      } // if
      else if (current != null) {
        if (current == this.front.get(lvl) && this.comparator.compare(current.key, key) == 0) {
          return current.value;
        } else if (current == this.front.get(lvl)
            && this.comparator.compare(current.key, key) > 0) {
          if (lvl > 0) {
            current = this.front.get(lvl - 1);
            getCount++;
          } // if not already at bottom level, move down a level
        } // else if
        else {
          getCount++;
          while (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
            getCount++;
            current = current.next.get(lvl);
            getCount++;
          } // while current < key at level lvl
          getCount++;
          if (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
            getCount++;
            current = current.next.get(lvl);
            getCount++;
            return current.value;
          } // if found key, update value
        } // else
      } // if current not null
    } // for each level
    throw new IndexOutOfBoundsException("key invalid: " + key);
  } // get(K,V)

  /**
   * Determine how many values are in the map.
   */
  @Override
  public int size() {
    return this.size;
  } // size()

  /**
   * Determine if a key appears in the table.
   */
  @Override
  public boolean containsKey(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    // Search SkipList for key
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current == null && lvl > 0) {
        current = this.front.get(lvl - 1);
      } // if
      else if (current != null) {
        if (current == this.front.get(lvl) && this.comparator.compare(current.key, key) == 0) {
          return true;
        } else if (current == this.front.get(lvl)
            && this.comparator.compare(current.key, key) > 0) {
          if (lvl > 0) {
            current = this.front.get(lvl - 1);
          } // if not already at bottom level, move down a level
        } // else if
        else {
          while (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
            current = current.next.get(lvl);
          } // while current < key at level lvl
          if (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
            current = current.next.get(lvl);
            return true;
          } // if found key, update value
        } // else
      } // if current not null
    } // for each level
    return false;
  } // containsKey(K)

  /**
   * Remove the value with the given key.
   * 
   * @return The associated value (or null, if there is no associated value).
   * @throws NullPointerException if the key is null.
   */
  @Override
  public V remove(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    // array of prev pointers
    ArrayList<SLNode<K, V>> prev = new ArrayList<SLNode<K, V>>(this.height);
    for (int i = 0; i < this.height; i++) {
      prev.add(null);
    } // for (initialize prev array)

    // Search SkipList for key
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current == null && lvl > 0) {
        current = this.front.get(lvl - 1);
        remCount++;
      } // if
      else if (current != null) {
        if (current == this.front.get(lvl) && this.comparator.compare(current.key, key) >= 0) {
          if (lvl > 0) {
            current = this.front.get(lvl - 1);
            remCount++;
          } // if not already at bottom level, move down a level
        } // else if
        else {
          remCount++;
          while (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
            remCount++;
            current = current.next.get(lvl);
            remCount++;
          } // while current < key at level lvl
          // prev.set(lvl, current); // add prev pointer to prev array
          remCount++;
          if (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
            remCount++;
            prev.set(lvl, current);
          } // if found key, update value
        } // else
      } // if current not null
    } // for each level

    // Remove value
    if (current == null) {
      return null;
    } // if key not found
    SLNode<K, V> temp;
    if (this.comparator.compare(current.key, key) == 0) {
      temp = current;
    } // if current is the key
    else {
      temp = current.next.get(0);
      remCount++;
    } // else
    if (temp != null && temp.next != null) {
      for (int lvl = 0; lvl < temp.next.size(); lvl++) {
        if (prev.get(lvl) != null) {
          prev.get(lvl).next.set(lvl, prev.get(lvl).next.get(lvl).next.get(lvl));
        } else if (this.comparator.compare(this.front.get(lvl).key, key) == 0) {
          this.front.set(lvl, this.front.get(lvl).next.get(lvl));
        } else {
          return null;
        } // else
      } // for
    } // if
    else {
      return null;
    } // if temp is null
    size--;
    return temp.value;
  } // remove(K)

  /**
   * Get an iterator for all of the keys in the map.
   */
  @Override
  public Iterator<K> keys() {
    return new Iterator<K>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public K next() {
        return nit.next().key;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // keys()

  /**
   * Get an iterator for all of the values in the map.
   */
  @Override
  public Iterator<V> values() {
    return new Iterator<V>() {
      Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

      @Override
      public boolean hasNext() {
        return nit.hasNext();
      } // hasNext()

      @Override
      public V next() {
        return nit.next().value;
      } // next()

      @Override
      public void remove() {
        nit.remove();
      } // remove()
    };
  } // values()

  /**
   * Apply a function to each key/value pair.
   */
  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Iterator<SLNode<K, V>> it = this.nodes();
    while (it.hasNext()) {
      SLNode<K, V> temp = it.next();
      action.accept(temp.key, temp.value);
    } // while
  } // forEach

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    String leading = "          ";

    SLNode<K, V> current = front.get(0);

    // Print some X's at the start
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" X");
    } // for
    pen.println();
    printLinks(pen, leading);

    while (current != null) {
      // Print out the key as a fixed-width field.
      // (There's probably a better way to do this.)
      String str;
      if (current.key == null) {
        str = "<null>";
      } else {
        str = current.key.toString();
      } // if/else
      if (str.length() < leading.length()) {
        pen.print(leading.substring(str.length()) + str);
      } else {
        pen.print(str.substring(0, leading.length()));
      } // if/else

      // Print an indication for the links it has.
      for (int level = 0; level < current.next.size(); level++) {
        pen.print("-*");
      } // for
      // Print an indication for the links it lacks.
      for (int level = current.next.size(); level < this.height; level++) {
        pen.print(" |");
      } // for
      pen.println();
      printLinks(pen, leading);

      current = current.next.get(0);
    } // while

    // Print some O's at the start
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" O");
    } // for
    pen.println();
  } // dump(PrintWriter)

  /**
   * Print some links (for dump).
   */
  void printLinks(PrintWriter pen, String leading) {
    pen.print(leading);
    for (int level = 0; level < this.height; level++) {
      pen.print(" |");
    } // for
    pen.println();
  } // printLinks

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Pick a random height for a new node.
   */
  int randomHeight() {
    int result = 1;
    while (rand.nextDouble() < prob) {
      result = result + 1;
    } // while
    return result;
  } // randomHeight()

  /**
   * Get an iterator for all of the nodes. (Useful for implementing the other iterators.)
   */
  Iterator<SLNode<K, V>> nodes() {
    return new Iterator<SLNode<K, V>>() {

      /**
       * A reference to the next node to return.
       */
      SLNode<K, V> next = SkipList.this.front.get(0);

      @Override
      public boolean hasNext() {
        return this.next != null;
      } // hasNext()

      @Override
      public SLNode<K, V> next() {
        if (this.next == null) {
          throw new IllegalStateException();
        }
        SLNode<K, V> temp = this.next;
        this.next = this.next.next.get(0);
        return temp;
      } // next();
    }; // new Iterator
  } // nodes()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

} // class SkipList


/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key.
   */
  K key;

  /**
   * The value.
   */
  V value;

  /**
   * Pointers to the next nodes.
   */
  ArrayList<SLNode<K, V>> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new node of height n with the specified key and value.
   */
  public SLNode(K key, V value, int n) {
    this.key = key;
    this.value = value;
    this.next = new ArrayList<SLNode<K, V>>(n);
    for (int i = 0; i < n; i++) {
      this.next.add(null);
    } // for
  } // SLNode(K, V, int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+
} // SLNode<K,V>
