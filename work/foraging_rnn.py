# Honey bee foraging RNN.
from numpy import array, argmax
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import TimeDistributed
from keras.layers import LSTM
import sys, getopt

# define LSTM configuration
n_neurons = 128
n_epochs = 1000

# get options
try:
  opts, args = getopt.getopt(sys.argv[1:],"hn:e:",["neurons=","epochs="])
except getopt.GetoptError:
  print('foraging_rnn.py [-n <neurons>] [-e <epochs>]')
  sys.exit(2)
for opt, arg in opts:
  if opt == '-h':
     print('foraging_rnn.py [-n <neurons>] [-e <epochs>]')
     sys.exit()
  if opt in ("-n", "--neurons"):
     n_neurons = int(arg)
  elif opt in ("-e", "--epochs"):
     n_epochs = int(arg)

# prepare data
from foraging_dataset import X_train_shape, X_train_seq, X_test_shape, X_test_seq, y_shape, y_seq
seq = array(X_train_seq)
X = seq.reshape(X_train_shape[0], X_train_shape[1], X_train_shape[2])
seq = array(y_seq)
y = seq.reshape(y_shape[0], y_shape[1], y_shape[2])
print('X train:\n', X)
print('y:\n', y)

# create LSTM
model = Sequential()
model.add(LSTM(n_neurons, input_shape=(X_train_shape[1], X_train_shape[2]), return_sequences=True))
model.add(TimeDistributed(Dense(y_shape[2])))
model.compile(loss='mean_squared_error', optimizer='adam')
print(model.summary())

# train
model.fit(X, y, epochs=n_epochs, batch_size=X_train_shape[0], verbose=2)

# evaluate
seq = array(X_test_seq)
X = seq.reshape(X_test_shape[0], X_test_shape[1], X_test_shape[2])
print('X test:\n', X)
predictions = model.predict(X, batch_size=X_test_shape[0], verbose=0)
print('results:')
for seq in range(X_test_shape[0]):
    print('sequence =', seq, 'predictions: ', end='')
    p = []
    for step in range(X_test_shape[1]):
        r = argmax(predictions[seq][step])
        p.append(r)
        print(r, ' ', end='')
    print('targets: ', end='')
    t = []
    for step in range(X_test_shape[1]):
        r = argmax(y[seq][step])
        t.append(r)
        print(r, ' ', end='')
    if p == t:
        print('OK')
    else:
        print('error')

