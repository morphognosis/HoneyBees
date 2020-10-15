# Honey bee foraging RNN.
from numpy import array, argmax
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import TimeDistributed
from keras.layers import LSTM
# prepare sequence
from foraging_dataset import X_shape, X_seq, y_shape, y_seq
seq = array(X_seq)
X = seq.reshape(X_shape[0], X_shape[1], X_shape[2])
seq = array(y_seq)
y = seq.reshape(y_shape[0], y_shape[1], y_shape[2])
print('X:\n', X)
print('y:\n', y)
# define LSTM configuration
n_neurons = 32
n_batch = X_shape[0]
n_epoch = 1000
# create LSTM
model = Sequential()
model.add(LSTM(n_neurons, input_shape=(X_shape[1], X_shape[2]), return_sequences=True))
model.add(TimeDistributed(Dense(y_shape[2])))
model.compile(loss='mean_squared_error', optimizer='adam')
print(model.summary())
# train LSTM
model.fit(X, y, epochs=n_epoch, batch_size=n_batch, verbose=2)
# evaluate
predictions = model.predict(X, batch_size=n_batch, verbose=0)
print('results:')
for seq in range(X_shape[0]):
    print('sequence =', seq, 'predictions: ', end='')
    p = []
    for step in range(X_shape[1]):
        r = argmax(predictions[seq][step])
        p.append(r)
        print(r, ' ', end='')
    print('targets: ', end='')
    t = []
    for step in range(X_shape[1]):
        r = argmax(y[seq][step])
        t.append(r)
        print(r, ' ', end='')
    if p == t:
        print('OK')
    else:
        print('error')




