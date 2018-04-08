<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()>
Partial Class Form2
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()>
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()>
    Private Sub InitializeComponent()
        Me.components = New System.ComponentModel.Container()
        Dim CBlendItems1 As CButtonLib.cBlendItems = New CButtonLib.cBlendItems()
        Dim resources As System.ComponentModel.ComponentResourceManager = New System.ComponentModel.ComponentResourceManager(GetType(Form2))
        Me.Button1 = New System.Windows.Forms.Button()
        Me.Button2 = New System.Windows.Forms.Button()
        Me.Button3 = New System.Windows.Forms.Button()
        Me.TextBox1 = New System.Windows.Forms.TextBox()
        Me.TextBox2 = New System.Windows.Forms.TextBox()
        Me.CButton1 = New CButtonLib.CButton()
        Me.CButton10 = New CButtonLib.CButton()
        Me.SuspendLayout()
        '
        'Button1
        '
        Me.Button1.Location = New System.Drawing.Point(356, 238)
        Me.Button1.Name = "Button1"
        Me.Button1.Size = New System.Drawing.Size(75, 23)
        Me.Button1.TabIndex = 2
        Me.Button1.Text = "Button1"
        Me.Button1.UseVisualStyleBackColor = True
        '
        'Button2
        '
        Me.Button2.Location = New System.Drawing.Point(437, 238)
        Me.Button2.Name = "Button2"
        Me.Button2.Size = New System.Drawing.Size(75, 23)
        Me.Button2.TabIndex = 3
        Me.Button2.Text = "Button2"
        Me.Button2.UseVisualStyleBackColor = True
        '
        'Button3
        '
        Me.Button3.Location = New System.Drawing.Point(528, 238)
        Me.Button3.Name = "Button3"
        Me.Button3.Size = New System.Drawing.Size(75, 23)
        Me.Button3.TabIndex = 4
        Me.Button3.Text = "Button3"
        Me.Button3.UseVisualStyleBackColor = True
        '
        'TextBox1
        '
        Me.TextBox1.Location = New System.Drawing.Point(357, 149)
        Me.TextBox1.Name = "TextBox1"
        Me.TextBox1.Size = New System.Drawing.Size(293, 20)
        Me.TextBox1.TabIndex = 0
        '
        'TextBox2
        '
        Me.TextBox2.Location = New System.Drawing.Point(357, 184)
        Me.TextBox2.Name = "TextBox2"
        Me.TextBox2.Size = New System.Drawing.Size(293, 20)
        Me.TextBox2.TabIndex = 1
        '
        'CButton1
        '
        Me.CButton1.AutoEllipsis = False
        Me.CButton1.AutoSizeMode = False
        Me.CButton1.DesignerSelected = False
        Me.CButton1.ImageIndex = 0
        Me.CButton1.ImageKey = 0
        Me.CButton1.Location = New System.Drawing.Point(624, 238)
        Me.CButton1.Name = "CButton1"
        Me.CButton1.Size = New System.Drawing.Size(90, 25)
        Me.CButton1.TabIndex = 5
        Me.CButton1.Text = "CButton1"
        Me.CButton1.UseVisualStyleBackColor = False
        '
        'CButton10
        '
        Me.CButton10.AutoEllipsis = False
        Me.CButton10.AutoSizeMode = False
        Me.CButton10.BackColor = System.Drawing.Color.Transparent
        Me.CButton10.BorderColor = System.Drawing.Color.Transparent
        Me.CButton10.BorderShow = False
        CBlendItems1.iColor = New System.Drawing.Color() {System.Drawing.Color.FromArgb(CType(CType(30, Byte), Integer), CType(CType(150, Byte), Integer), CType(CType(30, Byte), Integer)), System.Drawing.Color.FromArgb(CType(CType(255, Byte), Integer), CType(CType(255, Byte), Integer), CType(CType(255, Byte), Integer))}
        CBlendItems1.iPoint = New Single() {0!, 1.0!}
        Me.CButton10.ColorFillBlend = CBlendItems1
        Me.CButton10.ColorFillSolid = System.Drawing.Color.Brown
        Me.CButton10.DesignerSelected = False
        Me.CButton10.FillType = CButtonLib.CButton.eFillType.GradientPath
        Me.CButton10.FillTypeLinear = System.Drawing.Drawing2D.LinearGradientMode.ForwardDiagonal
        Me.CButton10.FocalPoints.FocusPtX = 0.6136364!
        Me.CButton10.FocalPoints.FocusPtY = 0.5!
        Me.CButton10.Font = New System.Drawing.Font("Arial", 12.0!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(0, Byte))
        Me.CButton10.ForeColor = System.Drawing.Color.Green
        Me.CButton10.ImageIndex = 0
        Me.CButton10.ImageKey = 0
        Me.CButton10.Location = New System.Drawing.Point(666, 149)
        Me.CButton10.Name = "CButton10"
        Me.CButton10.Padding = New System.Windows.Forms.Padding(25, 28, 19, 5)
        Me.CButton10.SideImage = CType(resources.GetObject("CButton10.SideImage"), System.Drawing.Image)
        Me.CButton10.SideImageAlign = System.Drawing.ContentAlignment.TopRight
        Me.CButton10.SideImageSize = New System.Drawing.Size(32, 36)
        Me.CButton10.Size = New System.Drawing.Size(160, 64)
        Me.CButton10.TabIndex = 14
        Me.CButton10.Text = "Add"
        Me.CButton10.TextMargin = New System.Windows.Forms.Padding(0)
        Me.CButton10.TextShadow = System.Drawing.Color.Silver
        Me.CButton10.UseVisualStyleBackColor = False
        '
        'Form2
        '
        Me.AcceptButton = Me.CButton1
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.BackColor = System.Drawing.SystemColors.ButtonHighlight
        Me.ClientSize = New System.Drawing.Size(1448, 895)
        Me.Controls.Add(Me.CButton10)
        Me.Controls.Add(Me.CButton1)
        Me.Controls.Add(Me.TextBox2)
        Me.Controls.Add(Me.TextBox1)
        Me.Controls.Add(Me.Button3)
        Me.Controls.Add(Me.Button2)
        Me.Controls.Add(Me.Button1)
        Me.Name = "Form2"
        Me.Text = "Form2"
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub

    Friend WithEvents Button1 As Button
    Friend WithEvents Button2 As Button
    Friend WithEvents Button3 As Button
    Friend WithEvents TextBox1 As TextBox
    Friend WithEvents TextBox2 As TextBox
    Friend WithEvents CButton1 As CButtonLib.CButton
    Friend WithEvents CButton10 As CButtonLib.CButton
End Class
