import React from 'react';
import {Platform, StatusBar, StyleSheet, View} from 'react-native';
import {SafeAreaProvider, useSafeAreaInsets} from 'react-native-safe-area-context';
import {PerseView} from './src/PerseView';

function App() {
  return (
    <SafeAreaProvider>
      <StatusBar barStyle="dark-content" />
      <AppContent />
    </SafeAreaProvider>
  );
}

function AppContent() {
  const insets = useSafeAreaInsets();
  return (
    <View style={[styles.container, {paddingTop: insets.top, paddingBottom: insets.bottom}]}> 
      {Platform.OS === 'android' ? <PerseView /> : <View style={styles.placeholder} />}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1},
  placeholder: {flex: 1, backgroundColor: '#EEE'},
});

export default App;
