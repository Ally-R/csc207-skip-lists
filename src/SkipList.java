import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

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
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current != null) {
        if (current == this.front.get(lvl)) {
          if (this.comparator.compare(current.key, key) == 0) {
            V temp = current.value;
            current.value = value;
            return temp;
          } else if (this.comparator.compare(current.key, key) > 0 && lvl > 0) {
            prev.set(lvl, current); // add prev pointer to prev array
            current = this.front.get(lvl - 1);
          } // else
        } // if
        else {
          while (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
            current = current.next.get(lvl);
          } // while current < key at level lvl
          prev.set(lvl, current); // add prev pointer to prev array
          if (current.next.get(lvl) != null
              && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
            current = current.next.get(lvl);
            V temp = current.value;
            current.value = value;
            return temp;
          } // if found key, update value
        } // else
      } // if current not null
      else if (lvl > 0) {
        current = this.front.get(lvl - 1);
      } // else
    } // for each level
    SLNode<K, V> setNode = new SLNode<K, V>(key, value, this.randomHeight());
    for (int lvl = this.height; lvl <= setNode.next.size(); lvl++) {
      this.front.add(setNode);
      setNode.next.set(lvl - 1, null);
    } // if node largest yet
    if (this.height < setNode.next.size()) {
      this.height = setNode.next.size();
    } // if (update height)
    for (int lvl = 0; lvl < setNode.next.size() && lvl < prev.size(); lvl++) {
      if (prev.get(lvl) == this.front.get(lvl) && (prev.get(lvl) == null || this.comparator.compare(prev.get(lvl).key, key) > 0)) {
        setNode.next.set(lvl, null);
        front.set(lvl, setNode);
      } /*else if (prev.get(lvl) == null) {
        setNode.next.set(lvl, front.get(lvl).next.get(lvl));
        front.get.next.set(lvl, setNode);
      } */ else {
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
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current != null) {
        while (current.next.get(lvl) != null
            && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
          current = current.next.get(lvl);
        } // while current < key at level lvl
        if (current.next.get(lvl) != null
            && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
          return current.next.get(lvl).value;
        } // if found key, return true
      } // if current not null
      else if (lvl > 0) {
        current = this.front.get(lvl - 1);
      } // else
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
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current != null) {
        if (current == front.get(lvl) && this.comparator.compare(current.key, key) == 0) {
          return true;
        } // if current key matches key
        while (current.next.get(lvl) != null
            && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
          current = current.next.get(lvl);
        } // while current < key at level lvl
        if (current.next.get(lvl) != null
            && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
          return true;
        } // if found key, return true
      } // if current not null
      else if (lvl > 0) {
        current = this.front.get(lvl - 1);
      } // else
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
    SLNode<K, V> current = this.front.get(this.height - 1);
    for (int lvl = this.height - 1; lvl >= 0; lvl--) {
      if (current != null) {
        while (current.next.get(lvl) != null
            && this.comparator.compare(current.next.get(lvl).key, key) < 0) {
          current = current.next.get(lvl);
        } // while current < key at level lvl
        if (current.next.get(lvl) != null
            && this.comparator.compare(current.next.get(lvl).key, key) == 0) {
          prev.set(lvl, current);
        } // if found key, update value
      } // if current not null
      else if (lvl > 0) {
        current = this.front.get(lvl - 1);
      } // else
    } // for each level
    V temp = current.next.get(0).value;
    //****************************************************************************** V temp = current.next.get(0).value NULL POINTER
    if (temp == null) {
      return null;
    } // if key not found
    for (int lvl = 0; lvl < current.next.size(); lvl++) {
      prev.get(lvl).next.set(lvl, prev.get(lvl).next.get(lvl).next.get(lvl));
    } // for
    size--;
    return temp;
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
