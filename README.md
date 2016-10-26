# AndroidForceView
Android力导向图，算法来自于D3.js的v3版本。

效果图：  
![图片](https://github.com/Amot-zpan/AndroidForceView/blob/master/art/androidforceview.gif)

详细的力导向图（力布局）的算法，请参阅 [d3-force](https://github.com/d3/d3-force/blob/master/README.md)

D3.js 是一个 JavaScript 库，主要用在前端网页中，所以在 Android 中也可以通过嵌套网页来实现力导向图的效果，但性能如何，没做过多测试。由于
D3.js 主要负责的是坐标计算，而并不管如何去绘制，官方给的示例是通过 SVG 或 Html5 的 Canvas 来绘制的，具体信息还是看官方解释吧 [D3.js](https://github.com/d3/d3)。

### Android 原生代码实现力导向图
#### 分析
力导向图由节点（Node）和连线（Link）组成，每个节点在运动过程中的任意一个时间点都有一个坐标，根据具体坐标就能绘制当前状态下的图。随着节点的坐标不断地变化，不停地重绘整个图，便能看到一个动态的效果，直到每个节点的坐标不再变化，整个图将保持在最后稳定状态，不再重绘。当某个节点被拖动，稳定状态被打破，整个图重复上面过程，重新到达一个新的稳定状态。

将每个节点当作一个带负电的点电荷，那么每个节点与其他所有节点都有电荷斥力，从而能使每个节点相互排斥。对于相关的节点之间有连线，能够互相拉住。另外，整个图有一个重心，这个重心为一个固定坐标，所有的节点都会受到重力的作用，但这里并没有使用物理上的重力公式，而只是一个类似效果的几何约束，防止节点逃逸。通过合力实现力的平衡，力的平衡状态也就是整个图的稳定状态。每个节点通过受力情况，来计算当前所处位置的坐标。

#### 1. 计算坐标
虽然每个节点的坐标都是计算得到的，但总有开始状态。开始状态下的坐标设置的是获取的随机数，这个随机数可以根据当前 View 的宽高来限定。

*`Force` 类中的 `getRandomPosition(int max)` 方法生成初始状态下的随机坐标。*

1. 遍历所有 Node，给每个 Node 设置一个随机获取的坐标。
2. 遍历所有 Link，计算每条 Link 的 sourceNode 和 targetNode 的距离 d，利用 α 值和 Link 的拉力，计算 sourceNode 和 targetNode 的距离的减少量，d 越大，此次减少的距离越大（效果类似拉长的橡皮筋，拉得越长，松手后单位时间回弹的距离越大），再根据 sourceNode 和 targetNode 的 weight （weight是节点的重量，节点的子节点越多，weight越大，越不容易被拉动）更新这两个节点的坐标。
```java
// 根据 link 两端节点的位置和 link 的拉力，以及节点的重量来更新节点的坐标
for (int i = 0; i < linkCount; i++) {
    FLink link = links.get(i);
    FNode sourceNode = link.source;
    FNode targetNode = link.target;
    float dx = targetNode.x - sourceNode.x;
    float dy = targetNode.y - sourceNode.y;
    double d = dx * dx + dy * dy;
    if (d > 0) {
        d = Math.sqrt(d);

        d = alpha * linkStrength(link) * (d - linkDistance(link)) / d;
        dx *= d;
        dy *= d;

        float k = sourceNode.weight * 1.0f / (targetNode.weight + sourceNode.weight);
        targetNode.x -= dx * k;
        targetNode.y -= dy * k;

        k = 1 - k;
        sourceNode.x += dx * k;
        sourceNode.y += dy * k;
    }
}
```
3. 遍历所有 Node，使每个节点向 View 中心聚拢。
```java
// 几何约束，使整个图向 View 的中心聚拢
float k = alpha * gravity;
if (k != 0) {
    int w = width / 2;
    int h = height / 2;
    for (int i = 0; i < nodeCount; i++) {
        FNode node = nodes.get(i);
        node.x += (w - node.x) * k;
        node.y += (h - node.y) * k;
    }
}
```
4. 将所有的节点按节点的坐标创建一棵四叉树。

（未完待续...）

### 程序运行过程中的调用逻辑
