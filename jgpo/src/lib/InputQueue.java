package lib;

public class InputQueue {
	private static final int INPUT_QUEUE_LENGTH = 128;
    private int head;
    private int tail;
    private int length;
    private int frame_delay;
    private boolean first_frame;
    private int last_user_added_frame;
    private int first_incorrect_frame;
    private int last_frame_requested;
    private int last_added_frame;
    private GameInput prediction;
    private final GameInput[] inputs;
    private GameInput currentInput;
    
    public InputQueue() {
        head = 0;
        tail = 0;
        length = 0;
        frame_delay = 2;
        first_frame = true;
        last_user_added_frame   = GameInput.NULL_FRAME;
        first_incorrect_frame   = GameInput.NULL_FRAME;
        last_frame_requested    = GameInput.NULL_FRAME;
        last_added_frame        = GameInput.NULL_FRAME;

        prediction = new GameInput(0, GameInput.NULL_FRAME);
        inputs = new GameInput[INPUT_QUEUE_LENGTH];
        for(int i = 0; i < inputs.length; i++) {
            inputs[i] = new GameInput(0,0);
        }
        currentInput = null;
    }

	public void addInput(GameInput gameInput) {
		last_user_added_frame = gameInput.getFrame();
		
		int new_frame = advanceQueueHead(gameInput.getFrame());
		if(new_frame != GameInput.NULL_FRAME) {
			addDelayedInputToQueue(gameInput, new_frame);
		}
		
		gameInput.setFrame(new_frame);
		currentInput = gameInput;
	}

	private void addDelayedInputToQueue(GameInput gameInput, int frame_number) {
		inputs[head] = new GameInput(gameInput.getInput(), frame_number);
		head = (head + 1) % INPUT_QUEUE_LENGTH;
		length++;
		first_frame = false;
		
		last_added_frame = frame_number;
		
		if(prediction.getFrame() != GameInput.NULL_FRAME) {
			if( first_incorrect_frame == GameInput.NULL_FRAME &&
				!prediction.equals(gameInput)) {
				first_incorrect_frame = frame_number;
			}
		
			if( prediction.getFrame() == last_frame_requested &&
				first_incorrect_frame == GameInput.NULL_FRAME) {
				prediction.setFrame(GameInput.NULL_FRAME);
			} else {
				prediction.incrementFrame();
			}
		}
	}

	private int advanceQueueHead(int frame) {
		int expected_frame = first_frame ? 0 : inputs[getPreviousFrame(head)].getFrame() + 1;
		frame += frame_delay;
		if(expected_frame > frame) {
			return GameInput.NULL_FRAME;
		}
		
		while(expected_frame < frame) {
			GameInput last_input = inputs[getPreviousFrame(head)];
			addDelayedInputToQueue(last_input, expected_frame);
			expected_frame++;
		}
		
		assert(frame == 0 || frame == inputs[getPreviousFrame(head)].getFrame() + 1);
		return frame;
	}

	private int getPreviousFrame(int offset) {
		return  offset == 0 ? INPUT_QUEUE_LENGTH - 1 : offset - 1;
	}

	public void discardConfirmedFrames(int frame) {
		if(last_frame_requested != GameInput.NULL_FRAME) {
			frame = Math.min(frame, last_frame_requested);
		}
		
		if(frame >= last_added_frame) {
			tail = head;
		} else {
			int offset = frame - inputs[tail].getFrame() + 1;
			tail = (tail + offset) % INPUT_QUEUE_LENGTH;
			length -= offset;
		}
	}

	public GameInput getInput(int requested_frame) {
		last_frame_requested = requested_frame;
		
		if(prediction.getFrame() == GameInput.NULL_FRAME) {
			int offset = requested_frame - inputs[tail].getFrame();
			if(offset >= 0 && offset < length) {
				offset = (offset + tail) % INPUT_QUEUE_LENGTH;
				return inputs[offset];
			}
		
			if(requested_frame == 0) {
				prediction.setInput(0);
			} else if(last_added_frame == GameInput.NULL_FRAME) {
				prediction.setInput(0);
			} else {
				prediction = new GameInput(inputs[getPreviousFrame(head)].getInput(), inputs[getPreviousFrame(head)].getFrame());
			}
			
			prediction.incrementFrame();
		}
		
		GameInput input = new GameInput(prediction.getInput(), prediction.getFrame());
		input.setFrame(requested_frame);
		return input;
	}

	public int getFirstIncorrectFrame() {
		return first_incorrect_frame;
	}

	public void resetPrediction(int frameCount) {
		prediction.setFrame(GameInput.NULL_FRAME);
		first_incorrect_frame = GameInput.NULL_FRAME;
		last_frame_requested = GameInput.NULL_FRAME;
	}
  
	public void setFrameDelay(int frameDelay) {
		this.frame_delay = frameDelay;
	}
}
