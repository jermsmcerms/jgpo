package lib;

public class RingBuffer<T> {
	public int size;

    private final int capacity;
    private final T[] ring;
    private int head;
    private int tail;

    public RingBuffer(int size) {
        this.capacity = size;
        ring = (T[])(new Object[this.capacity]);
        head = 0;
        tail = 0;
        size = 0;
    }

    public void push(T element) {
        assert size != (capacity - 1);
        ring[head] = element;
        head = (head + 1) % capacity;
        size++;
    }

    public void pop() {
        assert size != capacity;
        tail = (tail + 1) % capacity;
        size--;
    }

    public T front() {
        assert size != capacity;
        return ring[tail];
    }

    public T item(int i) {
        assert i < size;
        return ring[(tail + i) % capacity];
    }

    public int size() { return size; }
    public boolean empty() { return size == 0; }
}
