import {requireNativeComponent, ViewProps} from 'react-native';

type Props = ViewProps & {
  page?: number;
};

const NativePerseView = requireNativeComponent<Props>('PerseView');

export function PerseView(props: Props) {
  return <NativePerseView style={{flex: 1}} {...props} />;
}
